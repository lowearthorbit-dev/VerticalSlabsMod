package com.brainterminator.verticalslabs.datagen;

import com.brainterminator.verticalslabs.VerticalSlabsCommon;
import com.brainterminator.verticalslabs.handler.VanillaSlabs;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.function.BiConsumer;

/**
 * Loader-independent generation of blockstates and models for the vertical
 * slabs. Uses vanilla 26.2's client data model API, which both Fabric datagen
 * (fabric-model-provider entrypoint) and NeoForge datagen expose unchanged.
 *
 * <p>Each vertical slab gets a bottom/top model pair mirroring the textures of
 * the vanilla slab's own model, plus a blockstate dispatching on the slab type.
 */
public final class VerticalSlabModels {
    private VerticalSlabModels() {
    }

    /**
     * Generates the blockstate files, block models and item models for every
     * registered vertical slab.
     */
    public static void generate(BlockModelGenerators generators) {
        BiConsumer<Identifier, ModelInstance> modelOutput = generators.modelOutput;
        for (Block slab : VerticalSlabsCommon.allSlabs()) {
            createModels(slab, modelOutput);
            generators.blockStateOutput.accept(verticalSlabBlockState(slab));
        }
    }

    private static void createModels(Block slab, BiConsumer<Identifier, ModelInstance> modelOutput) {
        Block vanilla = VanillaSlabs.getVanillaOf(slab);
        if (vanilla == null) {
            return;
        }
        // Mirror vanilla's own slab model textures.
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(vanilla))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(vanilla))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(vanilla));
        ModelTemplates.SLAB_BOTTOM.create(slab, mapping, modelOutput);
        ModelTemplates.SLAB_TOP.create(slab, mapping, modelOutput);
    }

    private static BlockModelDefinitionGenerator verticalSlabBlockState(Block slab) {
        MultiVariant bottom = BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(slab));
        MultiVariant top = BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(slab, "_top"));
        return MultiVariantGenerator.dispatch(slab, bottom)
                .with(PropertyDispatch.modify(BlockStateProperties.SLAB_TYPE)
                        .select(SlabType.BOTTOM, BlockModelGenerators.NOP)
                        .select(SlabType.TOP, BlockModelGenerators.NOP));
    }
}