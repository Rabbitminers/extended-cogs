package com.rabbitminers.extendedgears.mixin;

import com.rabbitminers.extendedgears.base.util.MaterialHelpers;
import com.rabbitminers.extendedgears.cogwheels.materials.CogwheelMaterialManager;
import com.rabbitminers.extendedgears.mixin_interface.IDynamicMaterialBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BracketedKineticBlockEntity.class)
public class MixinBracketedKineticBlockEntity extends SimpleKineticBlockEntity implements IDynamicMaterialBlockEntity {
    @Unique
    public ResourceLocation material = RegisteredObjects.getKeyOrThrow(Blocks.SPRUCE_PLANKS);

    public MixinBracketedKineticBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public ResourceLocation getMaterial() {
        return material;
    }

    @Override
    public InteractionResult applyMaterialIfValid(ItemStack stack) {
        if (level == null || (level.isClientSide() && !isVirtual()))
            return InteractionResult.SUCCESS;
        @Nullable
        ResourceLocation material = MaterialHelpers.getModelKey(stack, this.material);
        if (material == null)
            return InteractionResult.PASS;
        this.material = material;
        notifyUpdate();
        level.levelEvent(2001, worldPosition, Block.getId(getBlockState()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void applyMaterial(ResourceLocation material) {
        this.material = material;
    }

    @Override
    public void lazyTick() {
        if (material == null || level == null) return;
        boolean shouldBreak = (Math.abs(speed) > CogwheelMaterialManager.getSpeedLimit(this.material))
                || (Math.abs(capacity) > CogwheelMaterialManager.getStressLimit(this.material));
        if (shouldBreak)
            level.destroyBlock(worldPosition, true);
    }

    protected void redraw() {
        if (level == null)
            return;
        if (!isVirtual())
            requestModelDataUpdate();
        if (hasLevel()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            level.getChunkSource()
                    .getLightEngine()
                    .checkBlock(worldPosition);
        }
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        ResourceLocation prevMaterial = material;
        if (!compound.contains("Material"))
            return;

        material = NBTHelper.readResourceLocation(compound, "Material");
        if (material == null)
            material = RegisteredObjects.getKeyOrThrow(Blocks.SPRUCE_PLANKS);

        if (clientPacket && prevMaterial != material)
            redraw();
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        NBTHelper.writeResourceLocation(compound, "Material", material);
    }
}
