package com.rabbitminers.extendedgears.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;


public class ExtendedCogwheelsRecipeProvider extends RecipeProvider {
    protected final List<GeneratedRecipe> all = new ArrayList<>();

    public ExtendedCogwheelsRecipeProvider(PackOutput generator) {
        super(generator);
    }

    public void registerRecipes(@NotNull Consumer<FinishedRecipe> wrapped) {
        all.forEach(c -> c.register(wrapped));
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> writer) {
        all.forEach(c -> c.register(writer));
    }

    protected static class I {
        static ItemLike andesite() {
            return AllItems.ANDESITE_ALLOY.get();
        }

        static ItemLike shaft() {
            return AllBlocks.SHAFT.get();
        }

        static TagKey<Item> planks() {
            return ItemTags.PLANKS;
        }

        static TagKey<Item> buttons() {
            return ItemTags.BUTTONS;
        }
    }

    protected GeneratedRecipe register(GeneratedRecipe recipe) {
        all.add(recipe);
        return recipe;
    }

    @FunctionalInterface
    public interface GeneratedRecipe {
        void register(Consumer<FinishedRecipe> consumer);
    }
}
