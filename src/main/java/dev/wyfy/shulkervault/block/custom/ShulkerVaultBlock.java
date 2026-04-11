package dev.wyfy.shulkervault.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.screen.custom.ShulkerVaultMenu;
import dev.wyfy.shulkervault.sound.ModSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
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
            level.playSound(null, pos, ModSoundEvents.VAULT_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
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
    }