package com.brainterminator.verticalslabs;

import com.brainterminator.verticalslabs.blocks.VerticalSlabBlock;
import com.brainterminator.verticalslabs.handler.VanillaSlabs;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;

public class VerticalSlabs implements net.fabricmc.api.ModInitializer {
    public static final String MODID = "verticalslabs";

    public static final List<Block> SLABS = new ArrayList<>();
    public static final List<BlockItem> SLAB_ITEMS = new ArrayList<>();

    @Override
    public void onInitialize() {
        for (Block vanillaSlab : VanillaSlabs.VANILLA_SLABS) {
            registerVerticalSlab(vanillaSlab);
        }

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(MODID, "verticalslabs_tab"),
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("creativetab.verticalslabs_tab"))
                        .icon(() -> SLAB_ITEMS.getFirst().getDefaultInstance())
                        .displayItems((parameters, output) -> {
                            for (BlockItem item : SLAB_ITEMS) {
                                output.accept(item);
                            }
                        }).build());
    }

    private static void registerVerticalSlab(Block vanillaSlab) {
        String name = VanillaSlabs.verticalSlabPath(vanillaSlab);
        Identifier blockId = Identifier.fromNamespaceAndPath(MODID, name);
        Block block = new VerticalSlabBlock(() -> vanillaSlab.defaultBlockState(),
                BlockBehaviour.Properties.ofFullCopy(vanillaSlab).setId(ResourceKey.create(Registries.BLOCK, blockId)));
        Registry.register(BuiltInRegistries.BLOCK, blockId, block);
        SLABS.add(block);

        Identifier itemId = Identifier.fromNamespaceAndPath(MODID, name);
        BlockItem item = new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, itemId)));
        Registry.register(BuiltInRegistries.ITEM, itemId, item);
        SLAB_ITEMS.add(item);
    }
}