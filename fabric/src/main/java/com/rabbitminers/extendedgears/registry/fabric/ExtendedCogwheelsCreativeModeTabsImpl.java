package com.rabbitminers.extendedgears.registry.fabric;

import com.rabbitminers.extendedgears.ExtendedCogwheels;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsBlocks;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsCreativeModeTabs;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsCreativeModeTabs.RegistrateDisplayItemsGenerator;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsCreativeModeTabs.TabInfo;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsCreativeModeTabs.Tabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import java.util.function.Supplier;

public class ExtendedCogwheelsCreativeModeTabsImpl {
    private static final TabInfo MAIN_TAB = register("main",
            () -> FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.extendedgears"))
                    .icon(() -> ExtendedCogwheelsBlocks.SHAFTLESS_COGWHEEL.asStack())
                    .displayItems(new RegistrateDisplayItemsGenerator(Tabs.MAIN))
                    .build());

    CreativeModeTab getBaseTab() {
        return MAIN_TAB.tab();
    }

    public static ResourceKey<CreativeModeTab> getBaseTabKey() {
        return MAIN_TAB.key();
    }

    private static TabInfo register(String name, Supplier<CreativeModeTab> supplier) {
        ResourceLocation id = ExtendedCogwheels.asResource(name);
        ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id);
        CreativeModeTab tab = supplier.get();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
        return new TabInfo(key, tab);
    }

    public static boolean isInCreativeTab(RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        return ExtendedCogwheels.registrate().isInCreativeTab(entry, tab);
    }

    public static void useBaseTab() {
        ExtendedCogwheels.registrate().setCreativeTab(ExtendedCogwheelsCreativeModeTabs.getBaseTabKey());
    }
}

