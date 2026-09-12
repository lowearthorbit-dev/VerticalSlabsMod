package com.brainterminator.verticalslabs;

import com.brainterminator.verticalslabs.VerticalSlabsCommon;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

/**
 * Fabric block loot tables. Uses Fabric's provider so validation is scoped to
 * the mod namespace (vanilla 26.2 validates every registered block otherwise).
 */
public class FabricBlockLootTables extends FabricBlockLootSubProvider {
    public FabricBlockLootTables(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        for (Block slab : VerticalSlabsCommon.allSlabs()) {
            this.dropSelf(slab);
        }
    }
}