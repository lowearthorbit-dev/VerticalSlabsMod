package de.lowearthorbit.verticalslabs.neoforge.datagen;

import de.lowearthorbit.verticalslabs.VerticalSlabs;
import de.lowearthorbit.verticalslabs.datagen.ModLootTableProvider;
import de.lowearthorbit.verticalslabs.datagen.ModRecipeProvider;
import de.lowearthorbit.verticalslabs.datagen.VerticalSlabAssetProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = VerticalSlabs.MODID)
public class DataGen {
    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        registerProviders(event);
    }

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        registerProviders(event);
    }

    /**
     * Every datagen run generates the complete set of files. The client and
     * server runs share the output cache and would otherwise delete each
     * other's files as stale, so both handlers register all providers.
     */
    private static void registerProviders(GatherDataEvent event) {
        event.addProvider(ModLootTableProvider.create(
                event.getGenerator().getPackOutput(), event.getLookupProvider(),
                NeoForgeBlockLootTables::new));
        event.addProvider(new ModRecipeProvider.Runner(
                event.getGenerator().getPackOutput(), event.getLookupProvider()));
        event.addProvider(new VerticalSlabAssetProvider(
                event.getGenerator().getPackOutput()));
        event.addProvider(new EnglishLangProvider(
                event.getGenerator().getPackOutput()));
    }
}