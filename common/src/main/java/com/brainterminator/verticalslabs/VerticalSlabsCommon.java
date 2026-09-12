package com.brainterminator.verticalslabs;

import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Loader-independent holder for the registered vertical slab blocks.
 * Each loader populates this list with lazy suppliers during registration.
 */
public final class VerticalSlabsCommon {
    public static final String MODID = "verticalslabs";

    private static final List<Supplier<Block>> SLABS = new ArrayList<>();

    private VerticalSlabsCommon() {
    }

    /** Called by the loader entrypoint for every registered vertical slab. */
    public static void addSlab(Supplier<Block> slab) {
        SLABS.add(slab);
    }

    /** All registered vertical slab blocks, resolved lazily. */
    public static List<Block> allSlabs() {
        return SLABS.stream().map(Supplier::get).toList();
    }
}