package dev.wyfy.shulkervault.block.custom;

import dev.wyfy.shulkervault.block.entity.AdvancedShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.block.entity.ModBlockEntities;
import dev.wyfy.shulkervault.screen.custom.AdvancedShulkerVaultMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Advanced Shulker Vault block with package slot support.
 * Extends base vault with Create package handling capabilities.
 */
public class AdvancedShulkerVaultBlock extends ShulkerVaultBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public AdvancedShulkerVaultBlock(Properties properties) {
        super(properties);
        // Build on parent's default state (which sets FACING), don't replace it
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }

        boolean previouslyPowered = state.getValue(POWERED);
        boolean currentlyPowered = level.hasNeighborSignal(pos);

        if (previouslyPowered == currentlyPowered) {
            return;
        }

        // Update block state
        level.setBlock(pos, state.setValue(POWERED, currentlyPowered), Block.UPDATE_CLIENTS);

        // Rising edge: trigger packaging
        if (!previouslyPowered && currentlyPowered) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AdvancedShulkerVaultBlockEntity advancedBE) {
                advancedBE.triggerPackaging();
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AdvancedShulkerVaultBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.ADVANCED_SHULKER_VAULT_BE.get()
                ? (lvl, pos, st, be) -> ((AdvancedShulkerVaultBlockEntity) be).tick()
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AdvancedShulkerVaultBlockEntity advancedBE)) {
            return InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inv, p) -> new AdvancedShulkerVaultMenu(containerId, inv, advancedBE),
                            state.getBlock().getName()
                    ),
                    buf -> buf.writeBlockPos(pos)
            );
        }

        return InteractionResult.CONSUME;
    }
}
