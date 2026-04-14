package dev.wyfy.shulkervault.block.entity;

import com.simibubi.create.foundation.utility.IInteractionChecker;
import dev.wyfy.shulkervault.Config;
import dev.wyfy.shulkervault.block.custom.ShulkerVaultBlock;
import dev.wyfy.shulkervault.sound.ModSoundEvents;
import dev.wyfy.shulkervault.storage.ShulkerVaultStorage;
import dev.wyfy.shulkervault.screen.custom.ShulkerVaultMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Block entity for ShulkerVault, closely following vanilla ShulkerBoxBlockEntity patterns.
 */
public class ShulkerVaultBlockEntity extends BlockEntity implements IInteractionChecker {

    // Animation constants (matching vanilla)
    public static final int OPENING_TICK_LENGTH = 10;
    public static final float MAX_LID_HEIGHT = 0.5F;

    private final ShulkerVaultStorage storage;

    // Animation state (vanilla pattern)
    private int openCount;
    private AnimationStatus animationStatus = AnimationStatus.CLOSED;
    private float progress;
    private float progressOld;

    // Item slot/hover animation
    private ItemStack clientDisplayItem = ItemStack.EMPTY;

    public void setClientDisplaySlot(ItemStack stack) {
        this.clientDisplayItem = stack;
    }

    public ItemStack getClientDisplayItem() {
        return this.clientDisplayItem;
    }

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

    public ItemStack toDroppedStack(HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(getBlockState().getBlock());
        saveToItem(stack, registries);
        return stack;
    }

    // ==================== Animation System (vanilla pattern) ====================

    /**
     * Static tick method called by the block's ticker.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, ShulkerVaultBlockEntity blockEntity) {
        blockEntity.updateAnimation(level, pos, state);
    }

    /**
     * Instance tick method for compatibility.
     */
    public void tick() {
        if (this.level != null) {
            updateAnimation(this.level, this.worldPosition, this.getBlockState());
        }
    }

    /**
     * Updates the animation state machine (matching vanilla ShulkerBoxBlockEntity.updateAnimation).
     */
    private void updateAnimation(Level level, BlockPos pos, BlockState state) {
        this.progressOld = this.progress;

        switch (this.animationStatus) {
            case CLOSED:
                this.progress = 0.0F;
                break;

            case OPENING:
                this.progress += 0.1F;
                if (this.progressOld == 0.0F) {
                    doNeighborUpdates(level, pos, state);
                }
                if (this.progress >= 1.0F) {
                    this.animationStatus = AnimationStatus.OPENED;
                    this.progress = 1.0F;
                    doNeighborUpdates(level, pos, state);
                }
                this.moveCollidedEntities(level, pos, state);
                break;

            case OPENED:
                this.progress = 1.0F;
                break;

            case CLOSING:
                this.progress -= 0.1F;
                if (this.progressOld == 1.0F) {
                    doNeighborUpdates(level, pos, state);
                }
                if (this.progress <= 0.0F) {
                    this.animationStatus = AnimationStatus.CLOSED;
                    this.progress = 0.0F;
                    doNeighborUpdates(level, pos, state);
                }
                break;
        }
    }

    /**
     * Moves entities that collide with the opening lid (vanilla pattern).
     * This is what makes players get pushed smoothly instead of ejected.
     */
    private void moveCollidedEntities(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ShulkerVaultBlock) {
            Direction direction = state.getValue(ShulkerVaultBlock.FACING);
            // Use vanilla Shulker's helper to get the delta AABB
            AABB aabb = Shulker.getProgressDeltaAabb(1.0F, direction, this.progressOld, this.progress).move(pos);
            List<Entity> list = level.getEntities(null, aabb);

            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                        entity.move(
                                MoverType.SHULKER_BOX,
                                new Vec3(
                                        (aabb.getXsize() + 0.01) * (double) direction.getStepX(),
                                        (aabb.getYsize() + 0.01) * (double) direction.getStepY(),
                                        (aabb.getZsize() + 0.01) * (double) direction.getStepZ()
                                )
                        );
                    }
                }
            }
        }
    }

    private static void doNeighborUpdates(Level level, BlockPos pos, BlockState state) {
        state.updateNeighbourShapes(level, pos, 3);
        level.updateNeighborsAt(pos, state.getBlock());
    }

    // ==================== Container Open/Close (vanilla pattern) ====================

    /**
     * Called when a player opens the container (vanilla pattern using blockEvent).
     * Only runs on server side - blockEvent syncs to client.
     */
    public void startOpen(Player player) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (!this.remove && !player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }

            this.openCount++;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);

            if (this.openCount == 1) {
                this.level.gameEvent(player, GameEvent.CONTAINER_OPEN, this.worldPosition);
                playSound(ModSoundEvents.VAULT_OPEN.get());
            }
        }
    }

    /**
     * Called when a player closes the container (vanilla pattern using blockEvent).
     * Only runs on server side - blockEvent syncs to client.
     */
    public void stopOpen(Player player) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (!this.remove && !player.isSpectator()) {
            this.openCount--;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);

            if (this.openCount <= 0) {
                this.level.gameEvent(player, GameEvent.CONTAINER_CLOSE, this.worldPosition);
                playSound(ModSoundEvents.VAULT_CLOSE.get());
            }
        }
    }

    /**
     * Handles block events for animation state sync (vanilla pattern).
     */
    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.openCount = type;
            if (type == 0) {
                this.animationStatus = AnimationStatus.CLOSING;
            }
            if (type == 1) {
                this.animationStatus = AnimationStatus.OPENING;
            }
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    private void playSound(SoundEvent soundEvent) {
        if (this.level == null) {
            return;
        }
        this.level.playSound(
                null,
                this.worldPosition,
                soundEvent,
                SoundSource.BLOCKS,
                0.5F,
                this.level.random.nextFloat() * 0.1F + 0.9F
        );
    }

    // ==================== Animation Getters ====================

    /**
     * Returns interpolated progress for rendering (vanilla pattern).
     */
    public float getProgress(float partialTick) {
        return Mth.lerp(partialTick, this.progressOld, this.progress);
    }

    /**
     * Returns raw progress for collision calculations.
     */
    public float getRawProgress() {
        return this.progress;
    }

    public AnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    public boolean isClosed() {
        return this.animationStatus == AnimationStatus.CLOSED;
    }

    /**
     * Returns the bounding box for collision (vanilla pattern using Shulker helper).
     * Uses raw progress for collision, not interpolated.
     */
    public AABB getBoundingBox(BlockState state) {
        return Shulker.getProgressAabb(1.0F, state.getValue(ShulkerVaultBlock.FACING), MAX_LID_HEIGHT * this.progress);
    }

    // ==================== IInteractionChecker ====================

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

    // ==================== NBT Serialization ====================

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

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return super.getUpdateTag(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ==================== Animation Status Enum ====================

    public enum AnimationStatus {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING
    }
}
