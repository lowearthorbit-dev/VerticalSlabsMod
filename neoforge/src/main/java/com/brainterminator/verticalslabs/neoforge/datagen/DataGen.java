package com.brainterminator.verticalslabs.neoforge.datagen;

import com.brainterminator.verticalslabs.VerticalSlabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = VerticalSlabs.MODID)
public class DataGen {
    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.addProvider(ModLootTableProvider.create(
                event.getGenerator().getPackOutput(), event.getLookupProvider()));
        event.addProvider(new ModRecipeProvider.Runner(
                event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.addProvider(new EnglishLangProvider(
                event.getGenerator().getPackOutput()));
    }
}