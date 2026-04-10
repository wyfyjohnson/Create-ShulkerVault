package dev.wyfy.shulkervault.block.entity;

import com.simibubi.create.foundation.utility.IInteractionChecker;
import dev.wyfy.shulkervault.Config;
import dev.wyfy.shulkervault.storage.ShulkerVaultStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ShulkerVaultBlockEntity extends BlockEntity implements IInteractionChecker {

    private final ShulkerVaultStorage storage;

    public ShulkerVaultBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHULKER_VAULT_BE.get(), pos, state);
        this.storage = new ShulkerVaultStorage(
                () -> Config.stackMultiplier,
                this::setChanged
        );
    }

    public ShulkerVaultStorage getStorage() {
        return storage;
    }

    @Override
    public boolean canPlayerUse(Player player) {
        if (this.level == null) return false;
        if (this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5
        ) <= 64.0;
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
            storage.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }
}