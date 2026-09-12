package com.brainterminator.verticalslabs.neoforge.datagen;

import com.brainterminator.verticalslabs.VerticalSlabs;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (Block slab : VerticalSlabs.allSlabs()) {
            this.dropSelf(slab);
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return VerticalSlabs.allSlabs();
    }
}