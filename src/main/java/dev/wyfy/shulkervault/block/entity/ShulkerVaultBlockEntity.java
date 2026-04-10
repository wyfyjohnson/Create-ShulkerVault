package dev.wyfy.shulkervault.block.entity;

import dev.wyfy.shulkervault.storage.ShulkerVaultStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ShulkerVaultBlockEntity extends BlockEntity {

    private ShulkerVaultStorage storage;

    public ShulkerVaultBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHULKER_VAULT_BE.get(), pos, state);
        this.storage = new ShulkerVaultStorage(27,
                ShulkerVaultStorage.DEFAULT_STACK_MULTIPLIER);
    }

    public ShulkerVaultStorage getStorage() {
        return storage;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", storage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            CompoundTag inventoryTag = tag.getCompound("Inventory");
            int multiplier =
                    ShulkerVaultStorage.readMultiplierFromNBT(inventoryTag,
                            ShulkerVaultStorage.DEFAULT_STACK_MULTIPLIER);
            this.storage = new ShulkerVaultStorage(27, multiplier);
            this.storage.deserializeNBT(registries,inventoryTag);
        }
    }
}
