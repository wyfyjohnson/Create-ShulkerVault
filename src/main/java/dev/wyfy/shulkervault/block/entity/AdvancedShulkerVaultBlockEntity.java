package dev.wyfy.shulkervault.block.entity;

import com.simibubi.create.content.logistics.box.PackageItem;
import dev.wyfy.shulkervault.block.ModBlocks;
import dev.wyfy.shulkervault.storage.AdvancedVaultItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Block entity for AdvancedShulkerVault.
 * Extends base vault with a dedicated package slot for Create package handling.
 */
public class AdvancedShulkerVaultBlockEntity extends ShulkerVaultBlockEntity {

    // Single slot for package storage (separate from main 27-slot inventory)
    private final ItemStackHandler packageSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return PackageItem.isPackage(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // Cached item handler for capability exposure (avoids per-tick allocations)
    private final AdvancedVaultItemHandler itemHandler = new AdvancedVaultItemHandler(this);

    // Outbox state: true if package slot contains a locally-made package (don't auto-unpack)
    private boolean holdingOutboxPackage = false;

    public AdvancedShulkerVaultBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_SHULKER_VAULT_BE.get(), pos, state);
    }

    public ItemStackHandler getPackageSlot() {
        return packageSlot;
    }

    /**
     * Returns the cached item handler for capability exposure.
     * Contains all package detection, unpacking, and routing logic.
     */
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public boolean isHoldingOutboxPackage() {
        return holdingOutboxPackage;
    }

    public void setHoldingOutboxPackage(boolean holding) {
        if (this.holdingOutboxPackage != holding) {
            this.holdingOutboxPackage = holding;
            this.setChanged();
        }
    }

    // ==================== Redstone Packaging ====================

    /**
     * Called by the block on redstone rising edge.
     * Attempts to create a package from vault contents.
     */
    public void triggerPackaging() {
        itemHandler.tryPackPackage();
    }

    // ==================== Event-Driven Buffer Draining ====================

    @Override
    public void setChanged() {
        super.setChanged();
        // Trigger auto-drain whenever inventory state changes
        // The handler's processing guard prevents infinite recursion
        if (itemHandler != null) {
            itemHandler.tryDrainPackageSlot();
        }
    }

    @Override
    public ItemStack toDroppedStack(HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(ModBlocks.ADVANCED_SHULKER_VAULT.get());
        saveToItem(stack, registries);
        return stack;
    }

    // ==================== NBT Serialization ====================

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("PackageSlot", packageSlot.serializeNBT(registries));
        tag.putBoolean("HoldingOutboxPackage", holdingOutboxPackage);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("PackageSlot")) {
            packageSlot.deserializeNBT(registries, tag.getCompound("PackageSlot"));
        }
        holdingOutboxPackage = tag.getBoolean("HoldingOutboxPackage");
    }
}
