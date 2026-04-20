package dev.wyfy.shulkervault.storage;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import dev.wyfy.shulkervault.block.entity.AdvancedShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.sound.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Package-aware item handler for AdvancedShulkerVault.
 * All package detection, unpacking, and routing logic lives here.
 * The block entity remains a dumb state container.
 */
public class AdvancedVaultItemHandler implements IItemHandler {

    private static final int MAIN_STORAGE_SLOTS = 27;

    private final AdvancedShulkerVaultBlockEntity blockEntity;

    // Guard against recursive calls during inventory changes
    private boolean isProcessing = false;

    public AdvancedVaultItemHandler(AdvancedShulkerVaultBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /**
     * Exposes the underlying block entity so UI slots can unwrap the proxy
     * and access the ShulkerVaultStorage for oversized stack limit calculations.
     */
    public AdvancedShulkerVaultBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    // ==================== Auto-Drain Logic ====================

    /**
     * Attempts to unpack any package sitting in the buffer slot.
     * Called automatically when inventory state changes (via setChanged).
     * Uses a processing guard to prevent infinite recursion.
     */
    public void tryDrainPackageSlot() {
        if (blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide()) {
            return;
        }

        // Prevent recursive calls
        if (isProcessing) {
            return;
        }

        ItemStack packageStack = blockEntity.getPackageSlot().getStackInSlot(0);

        // Slot is empty - reset outbox state (player or hopper took the package)
        if (packageStack.isEmpty()) {
            blockEntity.setHoldingOutboxPackage(false);
            return;
        }

        // Don't auto-unpack locally-made packages (outbox mode)
        if (blockEntity.isHoldingOutboxPackage()) {
            return;
        }

        // Not a valid package
        if (!PackageItem.isPackage(packageStack)) {
            return;
        }

        isProcessing = true;
        try {
            // Check if we can unpack (simulate first)
            if (tryUnpackPackage(packageStack, true)) {
                // Actually unpack
                tryUnpackPackage(packageStack, false);
                // Remove the package from the buffer slot
                blockEntity.getPackageSlot().extractItem(0, 1, false);
            }
        } finally {
            isProcessing = false;
        }
    }

    // ==================== Redstone-Triggered Packaging ====================

    /**
     * Attempts to create a package from vault contents on redstone activation.
     * Scans adjacent signs for an address and extracts up to 9 stacks.
     *
     * @return true if a package was successfully created and placed in the package slot
     */
    public boolean tryPackPackage() {
        // Package slot must be empty to receive the new package
        if (!blockEntity.getPackageSlot().getStackInSlot(0).isEmpty()) {
            return false;
        }

        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) {
            return false;
        }

        // CRITICAL: Set processing guard to prevent auto-drain during entire packaging operation
        isProcessing = true;
        try {
            // Read address from adjacent signs
            String signAddress = updateSignAddress();

            // Extract up to 9 stacks from main storage (Greedy Extraction)
            ShulkerVaultStorage storage = blockEntity.getStorage();
            List<ItemStack> extractedItems = new ArrayList<>();

            for (int i = 0; i < MAIN_STORAGE_SLOTS && extractedItems.size() < 9; i++) {
                // Keep pulling from the SAME slot until it's empty or the package is full
                while (extractedItems.size() < 9) {
                    ItemStack existing = storage.getStackInSlot(i);
                    if (existing.isEmpty()) {
                        break; // Move to the next vault slot
                    }

                    // Extract one full vanilla stack (64) at a time
                    int extractAmount = Math.min(existing.getCount(), existing.getMaxStackSize());
                    ItemStack extracted = storage.extractItem(i, extractAmount, false);

                    if (!extracted.isEmpty()) {
                        extractedItems.add(extracted);
                    } else {
                        break; // Safety break if extraction fails
                    }
                }
            }

            // Nothing to package
            if (extractedItems.isEmpty()) {
                return false;
            }

            // Create the package
            ItemStack packageStack = PackageItem.containing(extractedItems);

            // Apply sign address if found
            if (!signAddress.isEmpty()) {
                packageStack.set(AllDataComponents.PACKAGE_ADDRESS, signAddress);
            }

            // Set outbox flag and insert package (guard prevents auto-drain)
            blockEntity.setHoldingOutboxPackage(true);
            blockEntity.getPackageSlot().setStackInSlot(0, packageStack);

            // Play packaging sound
            ModSoundEvents.playPackageRouted(level, blockEntity.getBlockPos());

            // Notify a Frogport directly above to pull the new package
            wakeTheFrogs();

            return true;
        } finally {
            isProcessing = false;
        }
    }

    /**
     * Hands off the outbox package directly to a Frogport above the vault.
     *
     * The Frogport's tryPullingFrom() applies an address filter bypass only for
     * {@code PackagerItemHandler} instances. Since our handler isn't one, the
     * filter can block the pull. Instead we hand the package to the Frogport's
     * startAnimation() directly — the same mechanism the Packager ultimately
     * uses to launch packages onto the chain conveyor.
     *
     * If the Frogport accepts the package (animation starts), we clear the
     * package slot. If it can't (no target, busy, etc.), the package stays
     * in the slot for manual retrieval or a later attempt.
     */
    private void wakeTheFrogs() {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) return;

        BlockPos above = blockEntity.getBlockPos().above();
        if (!(level.getBlockEntity(above) instanceof FrogportBlockEntity port)) return;
        if (port.isAnimationInProgress()) return;

        ItemStack packageStack = blockEntity.getPackageSlot().getStackInSlot(0);
        if (packageStack.isEmpty() || !PackageItem.isPackage(packageStack)) return;

        // Hand the package to the Frogport — it checks its own target validity
        port.startAnimation(packageStack.copy(), true);

        // If the Frogport accepted, its animation is now in progress
        if (port.isAnimationInProgress()) {
            blockEntity.getPackageSlot().setStackInSlot(0, ItemStack.EMPTY);
            blockEntity.setHoldingOutboxPackage(false);
        }
    }

    /**
     * Scans all 6 adjacent blocks for a sign and reads its text as an address.
     * Mimics Create's Packager.getSign() behavior.
     *
     * @return the first valid address found, or empty string if none
     */
    private String updateSignAddress() {
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        if (level == null) {
            return "";
        }

        for (Direction direction : Direction.values()) {
            BlockEntity adjacent = level.getBlockEntity(pos.relative(direction));
            if (!(adjacent instanceof SignBlockEntity sign)) {
                continue;
            }

            // Check both front and back of the sign
            for (boolean front : new boolean[]{true, false}) {
                SignText text = sign.getText(front);
                StringBuilder address = new StringBuilder();

                for (Component component : text.getMessages(false)) {
                    String line = component.getString();
                    if (!line.isBlank()) {
                        if (address.length() > 0) {
                            address.append(" ");
                        }
                        address.append(line.trim());
                    }
                }

                if (address.length() > 0) {
                    return address.toString();
                }
            }
        }

        return "";
    }

    // ==================== IItemHandler Implementation ====================

    @Override
    public int getSlots() {
        // 27 main + 1 package slot
        return MAIN_STORAGE_SLOTS + 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot < MAIN_STORAGE_SLOTS) {
            return blockEntity.getStorage().getStackInSlot(slot);
        } else {
            return blockEntity.getPackageSlot().getStackInSlot(0);
        }
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        // Package detection and routing
        if (PackageItem.isPackage(stack)) {
            return handlePackageInsertion(stack, simulate);
        }

        // Non-package: insert into main storage only
        if (slot < MAIN_STORAGE_SLOTS) {
            return blockEntity.getStorage().insertItem(slot, stack, simulate);
        }

        // Don't allow non-packages into package slot
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < MAIN_STORAGE_SLOTS) {
            return blockEntity.getStorage().extractItem(slot, amount, simulate);
        } else {
            return blockEntity.getPackageSlot().extractItem(0, amount, simulate);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < MAIN_STORAGE_SLOTS) {
            return blockEntity.getStorage().getSlotLimit(slot);
        } else {
            return blockEntity.getPackageSlot().getSlotLimit(0);
        }
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot < MAIN_STORAGE_SLOTS) {
            return blockEntity.getStorage().isItemValid(slot, stack);
        } else {
            // Package slot only accepts packages
            return PackageItem.isPackage(stack);
        }
    }

    // ==================== Package Logic ====================

    /**
     * Handles insertion of a Create package.
     * First attempts to unpack into main storage, then falls back to package slot.
     */
    private ItemStack handlePackageInsertion(ItemStack packageStack, boolean simulate) {
        // Try to unpack first
        if (tryUnpackPackage(packageStack, simulate)) {
            return ItemStack.EMPTY; // Fully consumed
        }

        // Unpack failed - try routing to package slot
        return routeToPackageSlot(packageStack, simulate);
    }

    /**
     * Attempts to unpack a Create package into main storage.
     * Uses all-or-nothing: if ANY item doesn't fit, unpack fails entirely.
     *
     * @param packageStack the package to unpack
     * @param simulate if true, only check if unpacking would succeed
     * @return true if unpack succeeded (or would succeed in simulate mode)
     */
    private boolean tryUnpackPackage(ItemStack packageStack, boolean simulate) {
        ShulkerVaultStorage storage = blockEntity.getStorage();
        ItemStackHandler contents = PackageItem.getContents(packageStack);

        // Simulate pass - verify ALL items fit
        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack item = contents.getStackInSlot(i);
            if (!item.isEmpty()) {
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(storage, item.copy(), true);
                if (!remainder.isEmpty()) {
                    return false; // Can't fit everything - abort
                }
            }
        }

        // If simulating, we're done
        if (simulate) {
            return true;
        }

        // Actual pass - insert all items
        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack item = contents.getStackInSlot(i);
            if (!item.isEmpty()) {
                ItemHandlerHelper.insertItemStacked(storage, item.copy(), false);
            }
        }

        // Play unpack sound
        ModSoundEvents.playPackageUnpack(
                blockEntity.getLevel(),
                blockEntity.getBlockPos()
        );

        return true;
    }

    /**
     * Routes a package to the dedicated package slot.
     *
     * @param packageStack the package to store
     * @param simulate if true, only check if routing would succeed
     * @return remainder that couldn't be inserted (empty if fully inserted)
     */
    private ItemStack routeToPackageSlot(ItemStack packageStack, boolean simulate) {
        ItemStackHandler packageSlot = blockEntity.getPackageSlot();
        ItemStack remainder = packageSlot.insertItem(0, packageStack, simulate);

        // Play sound only on actual insertion (not simulate) and only if something was inserted
        if (!simulate && remainder.getCount() < packageStack.getCount()) {
            ModSoundEvents.playPackageRouted(
                    blockEntity.getLevel(),
                    blockEntity.getBlockPos()
            );
        }

        return remainder;
    }
}
