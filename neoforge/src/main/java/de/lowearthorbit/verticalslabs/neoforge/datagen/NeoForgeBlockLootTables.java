package de.lowearthorbit.verticalslabs.neoforge.datagen;

import de.lowearthorbit.verticalslabs.VerticalSlabsCommon;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/**
 * NeoForge-specific block loot tables. Overrides {@link #getKnownBlocks()} so
 * the loot table validation only covers the mod's own blocks; vanilla 26.2
 * does not expose that hook, which is why this lives on the NeoForge side.
 */
public class NeoForgeBlockLootTables extends BlockLootSubProvider {
    public NeoForgeBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        for (Block slab : VerticalSlabsCommon.allSlabs()) {
            this.dropSelf(slab);
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return VerticalSlabsCommon.allSlabs();
    }
}