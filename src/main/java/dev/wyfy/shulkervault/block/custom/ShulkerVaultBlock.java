package dev.wyfy.shulkervault.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.screen.custom.ShulkerVaultMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.RenderShape;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import dev.wyfy.shulkervault.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.List;
public class ShulkerVaultBlock extends Block implements IWrenchable, EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public ShulkerVaultBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ShulkerVaultBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.SHULKER_VAULT_BE.get()
                ? (lvl, pos, st, be) -> ((ShulkerVaultBlockEntity) be).tick()
                : null;
    }

    /**
     * Forward block events to the block entity (required for animation sync).
     * This is how vanilla ShulkerBoxBlock works.
     */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, param);
    }

    /**
     * Returns collision shape using vanilla pattern via Shulker.getProgressAabb.
     */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ShulkerVaultBlockEntity vault) {
            return Shapes.create(vault.getBoundingBox(state));
        }
        return Shapes.block();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ShulkerVaultBlockEntity vault) {
            return Shapes.create(vault.getBoundingBox(state));
        }
        return Shapes.block();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ShulkerVaultBlockEntity vaultBE)) {
            return InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            // Sound is now played by ContainerOpenersCounter in block entity
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inv, p) -> new ShulkerVaultMenu(containerId, inv, vaultBE),
                            state.getBlock().getName()
                    ),
                    buf -> buf.writeBlockPos(pos)
            );
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // NOTE: Create's PonderTooltipHandler will automatically inject "Hold [W] to Ponder" right above this later!

        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.empty());

            // Header: Storage
            tooltipComponents.add(Component.translatable("tooltip.shulkervault.header.storage").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal("  ").append(Component.translatable("tooltip.shulkervault.capacity")).withStyle(ChatFormatting.AQUA));
        } else {

            tooltipComponents.add(Component.literal("Hold ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("[Shift]").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" for Summary").withStyle(ChatFormatting.DARK_GRAY)));
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShulkerVaultBlockEntity vaultBE) {
                ItemStack drop = vaultBE.toDroppedStack(level.registryAccess());
                ItemEntity ie = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                ie.setDefaultPickUpDelay();
                level.addFreshEntity(ie);
            }
        }
       return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ShulkerVaultBlockEntity vaultBE) {
            return vaultBE.toDroppedStack(level.registryAccess());
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        // Client side: return success early
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        // Fire break event for claim protection compatibility
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return InteractionResult.SUCCESS;
        }

        {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShulkerVaultBlockEntity vaultBE) {
                ItemStack drop = vaultBE.toDroppedStack(level.registryAccess());
                player.getInventory().placeItemBackInInventory(drop);
            }
        }

        // Remove the block without loot table drops
        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
        level.destroyBlock(pos, false);

        // Play Create's wrench sound
        IWrenchable.playRemoveSound(level, pos);

        return InteractionResult.SUCCESS;
    }
}