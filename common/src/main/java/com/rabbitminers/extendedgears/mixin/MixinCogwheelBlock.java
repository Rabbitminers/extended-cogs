package com.rabbitminers.extendedgears.mixin;

import com.rabbitminers.extendedgears.cogwheels.CogwheelType;
import com.rabbitminers.extendedgears.mixin_interface.CogwheelTypeProvider;
import com.rabbitminers.extendedgears.mixin_interface.IDynamicMaterialBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof IDynamicMaterialBlockEntity dyn) || level.isClientSide) {
            return;
        }

        cir.setReturnValue(dyn.applyMaterialIfValid(player.getItemInHand(hand)));
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
