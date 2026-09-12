package com.brainterminator.verticalslabs.neoforge.datagen;

import com.brainterminator.verticalslabs.VerticalSlabs;
import com.brainterminator.verticalslabs.handler.VanillaSlabs;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Vertical Slabs Recipes";
        }
    }

    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        for (Block slab : VerticalSlabs.allSlabs()) {
            verticalSlabRecipe(slab);
        }
    }

    protected void verticalSlabRecipe(Block slab) {
        Block original = VanillaSlabs.getVanillaOf(slab);
        if (original == null) {
            return;
        }
        HolderGetter<Item> items = this.registries.lookupOrThrow(Registries.ITEM);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.BUILDING_BLOCKS, slab)
                .requires(original)
                .unlockedBy(getHasName(original), has(original))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(VerticalSlabs.MODID,
                                BuiltInRegistries.BLOCK.getKey(slab).getPath())));
    }
}