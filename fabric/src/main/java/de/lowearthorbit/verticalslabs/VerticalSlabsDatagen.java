package de.lowearthorbit.verticalslabs;

import de.lowearthorbit.verticalslabs.datagen.ModLootTableProvider;
import de.lowearthorbit.verticalslabs.datagen.ModRecipeProvider;
import de.lowearthorbit.verticalslabs.datagen.VerticalSlabAssetProvider;
import de.lowearthorbit.verticalslabs.datagen.VerticalSlabLangData;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import java.util.concurrent.CompletableFuture;

public class VerticalSlabsDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider((FabricDataGenerator.Pack.Factory<VerticalSlabAssetProvider>) VerticalSlabAssetProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<ModRecipeProvider.Runner>)
                (output, registries) -> new ModRecipeProvider.Runner(output, registries));
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<FabricBlockLootTables>)
                (output, registries) -> new FabricBlockLootTables(output, registries));
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<FabricLanguageProvider>)
                (output, registries) -> new FabricEnglishLangProvider(output, registries));
    }

    private static class FabricEnglishLangProvider extends FabricLanguageProvider {
        FabricEnglishLangProvider(FabricPackOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        public void generateTranslations(net.minecraft.core.HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
            VerticalSlabLangData.englishTranslations().forEach(translationBuilder::add);
        }
    }
}