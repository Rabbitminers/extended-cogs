package com.rabbitminers.extendedgears.cogwheels;

import com.rabbitminers.extendedgears.mixin_interface.CogwheelTypeProvider;
import com.rabbitminers.extendedgears.mixin_interface.IDynamicMaterialBlockEntity;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.utility.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
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
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult ray) {
        if (!player.mayBuild())
            return InteractionResult.PASS;
        do {
            if (level.isClientSide)
                break;
            BlockState newState;
            boolean isConsumable = true;
            boolean isLarge = ICogWheel.isLargeCog(state);
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.is(AllItems.ANDESITE_ALLOY.get())) {
                Direction blankFace = Direction.get(
                        state.getValue(AXIS_DIRECTION) ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE,
                        state.getValue(AXIS)
                );
                if (blankFace != ray.getDirection())
                    break;
                newState = isLarge ? AllBlocks.LARGE_COGWHEEL.getDefaultState()
                        : AllBlocks.COGWHEEL.getDefaultState();
            }
            else if (heldItem.is(AllItems.WRENCH.get())) {
                Direction shaftedFace = Direction.get(
                        state.getValue(AXIS_DIRECTION) ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE,
                        state.getValue(AXIS)
                );
                if (shaftedFace != ray.getDirection())
                    break;
                newState = isLarge ? ExtendedCogwheelsBlocks.LARGE_SHAFTLESS_COGWHEEL.getDefaultState()
                        : ExtendedCogwheelsBlocks.SHAFTLESS_COGWHEEL.getDefaultState();
                isConsumable = false;
            }
            else
                break;
            newState = newState.setValue(AXIS, state.getValue(AXIS))
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
            BlockEntity oldBe = level.getBlockEntity(pos);
            if (!(oldBe instanceof IDynamicMaterialBlockEntity oldDyn))
                return InteractionResult.FAIL;
            ResourceLocation material = oldDyn.getMaterial();
            level.setBlock(pos, newState, 3);
            if (!player.isCreative())
            {
                if (heldItem.is(AllItems.ANDESITE_ALLOY.get()))
                    heldItem.shrink(1);
                else if (heldItem.is(AllItems.WRENCH.get()))
                    player.addItem(new ItemStack(AllItems.ANDESITE_ALLOY.get()));
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof IDynamicMaterialBlockEntity newDyn))
                return InteractionResult.FAIL;
            newDyn.applyMaterial(material);
            return InteractionResult.SUCCESS;
        } while (false);
        return super.use(state, level, pos, player, hand, ray);
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
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS) && face.getAxisDirection() == directionFromValue(state.getValue(AXIS_DIRECTION));
    }

    @Override
    public CogwheelType getType() {
        return CogwheelType.HALF_SHAFT;
    }
}
