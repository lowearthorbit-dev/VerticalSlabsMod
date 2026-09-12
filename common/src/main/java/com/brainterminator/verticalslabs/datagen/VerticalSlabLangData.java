package com.brainterminator.verticalslabs.datagen;

import com.brainterminator.verticalslabs.VerticalSlabsCommon;
import com.brainterminator.verticalslabs.handler.VanillaSlabs;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loader-independent lang entry generation for the vertical slabs.
 * Each loader's lang provider feeds these entries into its own writer.
 */
public final class VerticalSlabLangData {
    private VerticalSlabLangData() {
    }

    /** All en_us translations, keyed by translation key. */
    public static Map<String, String> englishTranslations() {
        Map<String, String> translations = new LinkedHashMap<>();
        translations.put("creativetab.verticalslabs_tab", "Vertical Slabs");
        for (Block slab : VerticalSlabsCommon.allSlabs()) {
            Block vanilla = VanillaSlabs.getVanillaOf(slab);
            if (vanilla != null) {
                String vanillaName = VanillaSlabs.registryPath(vanilla)
                        .replace("_slab", "")
                        .replace('_', ' ');
                translations.put(slab.getDescriptionId(), "Vertical " + vanillaName);
            }
        }
        return translations;
    }
}