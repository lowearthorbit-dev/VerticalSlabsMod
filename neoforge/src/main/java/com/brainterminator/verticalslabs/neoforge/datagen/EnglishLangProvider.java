package com.brainterminator.verticalslabs.neoforge.datagen;

import com.brainterminator.verticalslabs.VerticalSlabs;
import com.brainterminator.verticalslabs.handler.VanillaSlabs;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class EnglishLangProvider extends LanguageProvider {
    public EnglishLangProvider(PackOutput output) {
        super(output, VerticalSlabs.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("creativetab.verticalslabs_tab", "Vertical Slabs");
        for (Block slab : VerticalSlabs.allSlabs()) {
            Block vanilla = VanillaSlabs.getVanillaOf(slab);
            if (vanilla != null) {
                String vanillaName = VanillaSlabs.registryPath(vanilla)
                        .replace("_slab", "")
                        .replace('_', ' ');
                add(slab, "Vertical " + vanillaName);
            }
        }
    }
}