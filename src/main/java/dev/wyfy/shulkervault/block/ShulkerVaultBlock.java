package dev.wyfy.shulkervault.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class ShulkerVaultBlock extends Block implements IWrenchable {
    // 1. Define the property that allows the block to face all 6 directions
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public ShulkerVaultBlock(Properties properties) {
        super(properties);
        // 2. Set a default state just in case the block is placed by a command
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // 3. Register the property with the block's state definition
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // 4. Tell the block how to orient itself when a player places it
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // getNearestLookingDirection().getOpposite() makes the "front" of the 3D model face the player
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }
}