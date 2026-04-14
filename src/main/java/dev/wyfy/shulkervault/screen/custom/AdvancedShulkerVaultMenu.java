package dev.wyfy.shulkervault.screen.custom;

import dev.wyfy.shulkervault.block.entity.AdvancedShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.screen.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Menu for AdvancedShulkerVault with 27 main slots + 1 package slot.
 */
public class AdvancedShulkerVaultMenu extends AbstractContainerMenu {
    public final AdvancedShulkerVaultBlockEntity blockEntity;
    private final Player player;

    // Slot counts
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    private static final int VAULT_INVENTORY_SLOT_COUNT = 27;
    private static final int PACKAGE_SLOT_COUNT = 1;

    // Slot indices (player inventory first, then vault, then package)
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int VAULT_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;
    private static final int PACKAGE_SLOT_INDEX = VAULT_FIRST_SLOT_INDEX + VAULT_INVENTORY_SLOT_COUNT;

    public AdvancedShulkerVaultBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    // Client constructor
    public AdvancedShulkerVaultMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // Server constructor
    public AdvancedShulkerVaultMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.ADVANCED_SHULKER_VAULT_MENU.get(), containerId);
        this.blockEntity = (AdvancedShulkerVaultBlockEntity) entity;
        this.player = inv.player;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addVaultInventory(this.blockEntity);
        addPackageSlot(this.blockEntity);

        // Notify block entity that player opened the container
        this.blockEntity.startOpen(this.player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.blockEntity.stopOpen(player);
    }

    private void addVaultInventory(AdvancedShulkerVaultBlockEntity entity) {
        // 3 rows of 9 slots for the main vault inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new OversizedSlotItemHandler(
                        entity.getStorage(),
                        col + row * 9,
                        8 + col * 18,
                        18 + row * 18
                ));
            }
        }
    }

    private void addPackageSlot(AdvancedShulkerVaultBlockEntity entity) {
        // Package slot at right side of GUI
        this.addSlot(new SlotItemHandler(entity.getPackageSlot(), 0, 178, 36) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                // Delegate to the handler's isItemValid for package validation
                return entity.getPackageSlot().isItemValid(0, stack);
            }
        });
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = this.slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot, try to merge into vault first, then package slot
            if (!moveItemStackTo(sourceStack, VAULT_FIRST_SLOT_INDEX, PACKAGE_SLOT_INDEX + PACKAGE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < PACKAGE_SLOT_INDEX + PACKAGE_SLOT_COUNT) {
            // This is a vault or package slot, merge into player inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
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
