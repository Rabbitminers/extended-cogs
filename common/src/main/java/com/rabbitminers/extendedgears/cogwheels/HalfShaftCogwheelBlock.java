package com.rabbitminers.extendedgears.cogwheels;

import com.rabbitminers.extendedgears.mixin_interface.CogwheelTypeProvider;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.foundation.utility.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import static com.rabbitminers.extendedgears.base.util.DirectionHelpers.directionFromValue;
import static com.rabbitminers.extendedgears.base.util.DirectionHelpers.isDirectionPositive;
import static com.rabbitminers.extendedgears.cogwheels.legacy.LegacyHalfShaftCogwheelBlock.shapeBuilder;

public class HalfShaftCogwheelBlock extends CogWheelBlock implements CogwheelTypeProvider {
    public VoxelShaper voxelShape = shapeBuilder(box(2.0D, 6.0D, 2.0D, 14.0D, 10.0D, 14.0D))
            .add(6.0D, 8, 6.0D, 10.0D, 16, 10.0D).forDirectional();
    public VoxelShaper largeVoxelShape = shapeBuilder(box(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D))
            .add(6.0D, 8, 6.0D, 10.0D, 16, 10.0D).forDirectional();

    public static final BooleanProperty AXIS_DIRECTION = BooleanProperty.create("axis_direction");

    protected HalfShaftCogwheelBlock(boolean large, Properties properties) {
        super(large, properties);

        registerDefaultState(this.defaultBlockState().setValue(AXIS_DIRECTION,
                isDirectionPositive(Direction.AxisDirection.POSITIVE)));
    }

    public static HalfShaftCogwheelBlock small(Properties properties) {
        return new HalfShaftCogwheelBlock(false, properties);
    }

    public static HalfShaftCogwheelBlock large(Properties properties) {
        return new HalfShaftCogwheelBlock(true, properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS_DIRECTION);
        super.createBlockStateDefinition(builder);
    }

    public static Direction.AxisDirection getAxisDirection(BlockState state) {
        return state.getValue(AXIS_DIRECTION) ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        if (!state.hasProperty(AXIS))
            return super.getShape(state, worldIn, pos, context);
        Direction dir = Direction.fromAxisAndDirection(state.getValue(AXIS),
                directionFromValue(state.getValue(AXIS_DIRECTION)));
        return isLargeCog() ? largeVoxelShape.get(dir) : voxelShape.get(dir);
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace().getOpposite();
        boolean isDirectionPosotive = isDirectionPositive(direction.getAxisDirection());
        Direction.Axis axisFromDirection = direction.getAxis();
        if (context.getPlayer() == null)
            return super.getStateForPlacement(context);
        return super.getStateForPlacement(context)
                .setValue(AXIS_DIRECTION, context.getPlayer()
                        .isShiftKeyDown() != isDirectionPosotive)
                .setValue(AXIS, axisFromDirection);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(AXIS))
            return originalState;
        Direction direction = VoxelShaper
                .axisAsFace(originalState.getValue(AXIS));
        if (!originalState.getValue(AXIS_DIRECTION))
            direction = direction.getOpposite();
        if (targetedFace.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            direction = direction.getClockWise(targetedFace.getAxis());
        else
            direction = direction.getCounterClockWise(targetedFace.getAxis());
        return originalState
                .setValue(AXIS, direction.getAxis())
                .setValue(AXIS_DIRECTION, direction.getAxisDirection() == Direction.AxisDirection.POSITIVE);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS) && face.getAxisDirection() == directionFromValue(state.getValue(AXIS_DIRECTION));
    }

    @Override
    public CogwheelType getType() {
        return CogwheelType.HALF_SHAFT;
    }
}
