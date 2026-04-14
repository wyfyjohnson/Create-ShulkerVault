package dev.wyfy.shulkervault.block.entity;

import dev.wyfy.shulkervault.Config;
import dev.wyfy.shulkervault.block.ModBlocks;
import dev.wyfy.shulkervault.storage.ShulkerVaultStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
            // TODO: Check if item is a Create package once package handling is implemented
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public AdvancedShulkerVaultBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_SHULKER_VAULT_BE.get(), pos, state);
    }

    public ItemStackHandler getPackageSlot() {
        return packageSlot;
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
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("PackageSlot")) {
            packageSlot.deserializeNBT(registries, tag.getCompound("PackageSlot"));
        }
    }
}
