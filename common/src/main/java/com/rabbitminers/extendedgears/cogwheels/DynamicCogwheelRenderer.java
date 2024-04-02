package com.rabbitminers.extendedgears.cogwheels;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.StitchedSprite;
import com.rabbitminers.extendedgears.cogwheels.materials.ClientCogwheelMaterial;
import com.rabbitminers.extendedgears.cogwheels.materials.CogwheelMaterialManager;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsPartials;
import com.simibubi.create.foundation.model.BakedModelHelper;
import com.simibubi.create.foundation.render.SuperByteBufferCache.Compartment;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DynamicCogwheelRenderer {
    public static final Compartment<CogwheelModelKey> COGWHEEL = new Compartment<>();

    public static final StitchedSprite STRIPPED_LOG_TEMPLATE = new StitchedSprite(new ResourceLocation("block/stripped_spruce_log"));
    public static final StitchedSprite STRIPPED_LOG_TOP_TEMPLATE = new StitchedSprite(new ResourceLocation("block/stripped_spruce_log_top"));

    public static final String[] LOG_SUFFIXES = new String[]{"_log", "_stem", "_block"};

    public static @NotNull BakedModel generateModel(CogwheelModelKey key) {
        ClientCogwheelMaterial material = CogwheelMaterialManager.clientOf(key.material());
        PartialModel model = material != null ? material.getModel(key.large())
                : standardCogwheelModel(key.large());
        return generateModel(model.get(), key.material());
    }

    public static PartialModel standardCogwheelModel(boolean isLarge) {
        return isLarge ? ExtendedCogwheelsPartials.LARGE_COGWHEEL : ExtendedCogwheelsPartials.COGWHEEL;
    }

    public static BakedModel generateModel(BakedModel template, ResourceLocation id) {
        Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
        String path = id.getPath();

        if (path.endsWith("_planks")) {
            String namespace = id.getNamespace();
            String wood = path.substring(0, path.length() - 7);
            BlockState logBlockState = getStrippedLogState(namespace, wood);

            map.put(STRIPPED_LOG_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.SOUTH));
            map.put(STRIPPED_LOG_TOP_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.UP));
        } else {
            ClientCogwheelMaterial material = CogwheelMaterialManager.clientOf(id);
            if (material == null)
                return BakedModelHelper.generateModel(template, sprite -> null);
            material.textures().forEach((old, replacement) -> map.put(old.get(), replacement.get()));
        }

        return BakedModelHelper.generateModel(template, map::get);
    }

    public static BlockState getStrippedLogState(String namespace, String wood) {
        for (String suffix : LOG_SUFFIXES) {
            Optional<BlockState> state =
                    Registry.BLOCK.getHolder(ResourceKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(namespace, "stripped_" + wood + suffix)))
                            .map(Holder::value)
                            .map(Block::defaultBlockState);
            if (state.isPresent())
                return state.get();
        }
        return Blocks.STRIPPED_OAK_LOG.defaultBlockState();
    }

    public static TextureAtlasSprite getSpriteOnSide(BlockState state, Direction side) {
        BakedModel model = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(state);

        // Should never happen but seems to occur at runtime?
        if (model == null) {
            return null;
        }

        RandomSource random = RandomSource.create();
        random.setSeed(42L);

        List<BakedQuad> quads = model.getQuads(state, side, random);
        if (!quads.isEmpty()) {
            return quads.get(0)
                    .getSprite();
        }
        random.setSeed(42L);
        quads = model.getQuads(state, null, random);
        if (!quads.isEmpty()) {
            for (BakedQuad quad : quads) {
                if (quad.getDirection() == side) {
                    return quad.getSprite();
                }
            }
        }
        return model.getParticleIcon();
    }
}
