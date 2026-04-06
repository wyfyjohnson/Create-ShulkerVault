package dev.wyfy.shulkervault.block.custom;

import dev.wyfy.shulkervault.sound.ModSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class AdvancedShulkerVaultBlock extends ShulkerVaultBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public AdvancedShulkerVaultBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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