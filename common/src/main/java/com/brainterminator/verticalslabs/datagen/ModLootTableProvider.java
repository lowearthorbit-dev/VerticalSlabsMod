package com.brainterminator.verticalslabs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Loot table provider factory shared by both loaders. The block loot subclass
 * is injected so each loader can apply its own validation hooks
 * (NeoForge exposes {@code getKnownBlocks()}, vanilla does not).
 */
public class ModLootTableProvider {
    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
            Function<HolderLookup.Provider, BlockLootSubProvider> blockLootFactory) {
        return new LootTableProvider(output, Set.of(),
                List.of(new SubProviderEntry(blockLootFactory::apply, LootContextParamSets.BLOCK)),
                registries);
    }
}