package de.lowearthorbit.verticalslabs.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader-independent discovery of the vanilla slabs.
 * <p>
 * Slabs are found by scanning the static {@link net.minecraft.world.level.block.Blocks} fields whose
 * name ends in {@code _SLAB}; the vertical counterpart is derived from the registry key instead of
 * the translated display name (which is not available outside the client).
 */
public class VanillaSlabs {

    /** All vanilla slab blocks, ordered by their registry key. */
    public static final List<Block> VANILLA_SLABS = discoverVanillaSlabs();

    private static List<Block> discoverVanillaSlabs() {
        List<Block> slabs = new ArrayList<>();
        for (Field field : Blocks.class.getFields()) {
            if (field.getType() == Block.class && field.getName().endsWith("_SLAB")) {
                try {
                    slabs.add((Block) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        slabs.sort(java.util.Comparator.comparing(VanillaSlabs::registryPath));
        return slabs;
    }

    /** The registry path of a block, e.g. {@code "oak_slab"}. */
    public static String registryPath(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    /** The registry path of the vertical slab derived from a vanilla slab, e.g. {@code "oak_vertical_slab"}. */
    public static String verticalSlabPath(Block vanillaSlab) {
        return registryPath(vanillaSlab).replace("_slab", "_vertical_slab");
    }

    /** The vanilla slab a vertical slab was derived from, or {@code null}. */
    public static Block getVanillaOf(Block verticalSlab) {
        String path = BuiltInRegistries.BLOCK.getKey(verticalSlab).getPath();
        if (!path.endsWith("_vertical_slab")) {
            return null;
        }
        String vanillaPath = path.replace("_vertical_slab", "_slab");
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft", vanillaPath));
    }
}