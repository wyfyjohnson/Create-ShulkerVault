package dev.wyfy.shulkervault.screen.custom;

import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.screen.ModMenuTypes;
import dev.wyfy.shulkervault.storage.ShulkerVaultStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ShulkerVaultMenu extends AbstractContainerMenu {
    public final ShulkerVaultBlockEntity blockEntity;
    private final Player player;

    public ShulkerVaultBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    // Client constructor
    public ShulkerVaultMenu(int pContainerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // Server constructor
    public ShulkerVaultMenu(int pContainerId, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.SHULKER_VAULT_MENU.get(), pContainerId);
        this.blockEntity = (ShulkerVaultBlockEntity) entity;
        this.player = inv.player;

        addBlockEntityInventory(this.blockEntity);
        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Notify block entity that player opened the container
        this.blockEntity.startOpen(this.player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.blockEntity.stopOpen(player);
    }

    private void addBlockEntityInventory(ShulkerVaultBlockEntity entity) {
        // 3 rows of 9 slots for the Vault
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
//                this.addSlot(new SlotItemHandler(entity.getStorage(), col + row * 9, 8 + col * 18, 18 + row * 18));
                this.addSlot(new OversizedSlotItemHandler(entity.getStorage(), col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    // Slot layout: Vault slots 0-26, then player inventory 27-53, then hotbar 54-62
    private static final int TE_INVENTORY_SLOT_COUNT = 27;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 0;
    private static final int VANILLA_FIRST_SLOT_INDEX = TE_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_SLOT_COUNT = 36;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = this.slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Vault slot clicked (indices 0-26) → move to player inventory
        if (pIndex < TE_INVENTORY_SLOT_COUNT) {
            // For oversized stacks: split off one vanilla stack's worth
            int toMove = Math.min(sourceStack.getCount(), sourceStack.getMaxStackSize());
            ItemStack splitStack = sourceStack.copyWithCount(toMove);

            if (!moveItemStackTo(splitStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            // Calculate how many actually moved
            int moved = toMove - splitStack.getCount();
            if (moved > 0) {
                sourceStack.shrink(moved);
            }
        }
        // Player inventory slot clicked (indices 27-62) → move to vault
        else if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            ShulkerVaultStorage storage = blockEntity.getStorage();
            ItemStack remaining = sourceStack.copy();

            // Custom insertion loop to respect oversized slot limits
            for (int i = 0; i < TE_INVENTORY_SLOT_COUNT && !remaining.isEmpty(); i++) {
                remaining = storage.insertItem(i, remaining, false);
            }

            // Calculate what was actually moved and update the source slot
            if (remaining.getCount() != sourceStack.getCount()) {
                sourceStack.setCount(remaining.getCount());
            } else {
                return ItemStack.EMPTY; // Nothing moved
            }
        } else {
            return ItemStack.EMPTY;
        }

        // Update slot state
        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.blockEntity != null && this.blockEntity.canPlayerUse(pPlayer);
    }

    public void handleCtrlShiftClick(int slotIndex) {
        // Validate slot index
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return;
        }

        Slot clickedSlot = this.slots.get(slotIndex);
        if (clickedSlot == null || !clickedSlot.hasItem()) {
            return;
        }

        final int VAULT_SLOT_COUNT = 27;
        boolean isVaultSlot = slotIndex < VAULT_SLOT_COUNT;

        ShulkerVaultStorage storage = blockEntity.getStorage();

        if (isVaultSlot) {
            // === VAULT → PLAYER ===
            int storageSlot = slotIndex; // Direct 1:1 mapping

            // Extract entire oversized stack from vault
            ItemStack toMove = storage.extractItem(storageSlot, Integer.MAX_VALUE, false);
            if (toMove.isEmpty()) {
                return;
            }

            // Insert into player slots (indices 27+), respecting vanilla limits
            ItemStack remaining = toMove;
            for (int i = VAULT_SLOT_COUNT; i < this.slots.size() && !remaining.isEmpty(); i++) {
                Slot destSlot = this.slots.get(i);

                if (!destSlot.mayPlace(remaining)) {
                    continue;
                }

                ItemStack destStack = destSlot.getItem();
                int maxStackSize = Math.min(remaining.getMaxStackSize(), destSlot.getMaxStackSize(remaining));

                if (destStack.isEmpty()) {
                    // Empty slot: place up to vanilla limit
                    int toInsert = Math.min(remaining.getCount(), maxStackSize);
                    destSlot.set(remaining.split(toInsert));
                } else if (ItemStack.isSameItemSameComponents(destStack, remaining)) {
                    // Matching stack: merge up to vanilla limit
                    int space = maxStackSize - destStack.getCount();
                    int toInsert = Math.min(remaining.getCount(), space);
                    if (toInsert > 0) {
                        destStack.grow(toInsert);
                        remaining.shrink(toInsert);
                        destSlot.setChanged();
                    }
                }
            }

            // Return any remainder back to the vault
            if (!remaining.isEmpty()) {
                ItemStack leftover = storage.insertItem(storageSlot, remaining, false);
                // Fallback: try any vault slot if original is full
                for (int i = 0; i < VAULT_SLOT_COUNT && !leftover.isEmpty(); i++) {
                    leftover = storage.insertItem(i, leftover, false);
                }
            }

        } else {
            // === PLAYER → VAULT ===
            ItemStack toMove = clickedSlot.getItem().copy();
            clickedSlot.set(ItemStack.EMPTY);

            // Insert into vault slots, respecting oversized limits
            ItemStack remaining = toMove;
            for (int i = 0; i < VAULT_SLOT_COUNT && !remaining.isEmpty(); i++) {
                remaining = storage.insertItem(i, remaining, false);
            }

            // Return any remainder back to the player slot
            if (!remaining.isEmpty()) {
                clickedSlot.set(remaining);
            }

            clickedSlot.setChanged();
        }

        // Sync to client
        this.broadcastChanges();
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}