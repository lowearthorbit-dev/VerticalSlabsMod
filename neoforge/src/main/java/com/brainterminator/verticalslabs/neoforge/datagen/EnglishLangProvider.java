package com.brainterminator.verticalslabs.neoforge.datagen;

import com.brainterminator.verticalslabs.VerticalSlabsCommon;
import com.brainterminator.verticalslabs.datagen.VerticalSlabLangData;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class EnglishLangProvider extends LanguageProvider {
    public EnglishLangProvider(PackOutput output) {
        super(output, VerticalSlabsCommon.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        VerticalSlabLangData.englishTranslations().forEach(this::add);
    }
}