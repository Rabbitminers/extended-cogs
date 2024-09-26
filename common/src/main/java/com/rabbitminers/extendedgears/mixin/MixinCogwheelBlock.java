package com.rabbitminers.extendedgears.mixin;

import com.rabbitminers.extendedgears.cogwheels.CogwheelType;
import com.rabbitminers.extendedgears.mixin_interface.CogwheelTypeProvider;
import com.rabbitminers.extendedgears.mixin_interface.IDynamicMaterialBlockEntity;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CogWheelBlock.class)
public class MixinCogwheelBlock extends AbstractSimpleShaftBlock implements CogwheelTypeProvider {
    public MixinCogwheelBlock(Properties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At("TAIL"), cancellable = true)
    public void use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray,
                    CallbackInfoReturnable<InteractionResult> cir) {
        tryExtractShaft(state, level, pos, player, hand, ray);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IDynamicMaterialBlockEntity dyn) || level.isClientSide)
            return;
        cir.setReturnValue(dyn.applyMaterialIfValid(player.getItemInHand(hand)));
    }

    public boolean tryExtractShaft(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        if (player == null || level == null || level.isClientSide())
            return false;
        ItemStack heldItem = player.getItemInHand(hand);
        Direction face = ray.getDirection();
        if (heldItem.is(AllItems.WRENCH.get().asItem())) {
            if (face.getAxis() != state.getValue(RotatedPillarKineticBlock.AXIS))
                return false;
            if (!state.is(AllBlocks.COGWHEEL.get()) && !state.is(AllBlocks.LARGE_COGWHEEL.get()))
                return false;
            boolean isLarge = ICogWheel.isLargeCog(state);
            BlockState newState = isLarge ? ExtendedCogwheelsBlocks.LARGE_SHAFTLESS_COGWHEEL.getDefaultState()
                    : ExtendedCogwheelsBlocks.SHAFTLESS_COGWHEEL.getDefaultState();
            newState = newState
                    .setValue(RotatedPillarKineticBlock.AXIS, state.getValue(RotatedPillarKineticBlock.AXIS))
                    .setValue(ProperWaterloggedBlock.WATERLOGGED, state.getValue(ProperWaterloggedBlock.WATERLOGGED));
            BlockEntity oldBe = level.getBlockEntity(pos);
            if (!(oldBe instanceof IDynamicMaterialBlockEntity oldDyn))
                return false;
            ResourceLocation material = oldDyn.getMaterial();
            level.setBlock(pos, newState, 3);
            if (!player.isCreative())
                player.addItem(new ItemStack(AllBlocks.SHAFT.get().asItem()));
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IDynamicMaterialBlockEntity newDyn)
                newDyn.applyMaterial(material);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public CogwheelType getType() {
        return CogwheelType.STANDARD;
    }
}
