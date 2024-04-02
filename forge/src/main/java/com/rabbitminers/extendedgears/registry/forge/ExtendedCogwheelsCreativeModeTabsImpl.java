package com.rabbitminers.extendedgears.registry.forge;

import com.rabbitminers.extendedgears.ExtendedCogwheels;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsBlocks;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsCreativeModeTabs;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsCreativeModeTabs.RegistrateDisplayItemsGenerator;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsCreativeModeTabs.Tabs;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsTags;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExtendedCogwheelsCreativeModeTabsImpl {

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExtendedCogwheels.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TAB_REGISTER.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.extendedgears"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> ExtendedCogwheelsBlocks.SHAFTLESS_COGWHEEL.asStack())
                    .displayItems(new RegistrateDisplayItemsGenerator(Tabs.MAIN))
                    .build());

    public static void register(IEventBus modEventBus) {
        TAB_REGISTER.register(modEventBus);
    }

    public static CreativeModeTab getBaseTab() {
        return MAIN_TAB.get();
    }

    public static ResourceKey<CreativeModeTab> getBaseTabKey() {
        return MAIN_TAB.getKey();
    }

    public static boolean isInCreativeTab(RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        RegistryObject<CreativeModeTab> tabObject = MAIN_TAB;
        return ExtendedCogwheels.registrate().isInCreativeTab(entry, tabObject);
    }

    public static void useBaseTab() {
        ExtendedCogwheels.registrate().setCreativeTab(MAIN_TAB);
    }
}
