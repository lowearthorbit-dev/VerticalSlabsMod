package de.lowearthorbit.verticalslabs;

import de.lowearthorbit.verticalslabs.blocks.VerticalSlabBlock;
import de.lowearthorbit.verticalslabs.handler.VanillaSlabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

@Mod(VerticalSlabs.MODID)
public class VerticalSlabs {
    public static final String MODID = "verticalslabs";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final List<DeferredBlock<Block>> SLABS = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> SLAB_ITEMS = new ArrayList<>();

    static {
        for (Block vanillaSlab : VanillaSlabs.VANILLA_SLABS) {
            registerVerticalSlab(vanillaSlab);
        }
    }

    private static void registerVerticalSlab(Block vanillaSlab) {
        String name = VanillaSlabs.verticalSlabPath(vanillaSlab);
        DeferredBlock<Block> block = BLOCKS.registerBlock(name,
                properties -> new VerticalSlabBlock(() -> vanillaSlab.defaultBlockState(), properties),
                () -> BlockBehaviour.Properties.ofFullCopy(vanillaSlab));
        SLABS.add(block);
        SLAB_ITEMS.add(ITEMS.registerSimpleBlockItem(name, block));
        VerticalSlabsCommon.addSlab(block);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VERTICAL_SLABS_TAB =
            CREATIVE_MODE_TABS.register("verticalslabs_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.verticalslabs_tab"))
                    .icon(() -> SLAB_ITEMS.getFirst().get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        for (DeferredItem<BlockItem> item : SLAB_ITEMS) {
                            output.accept(item.get());
                        }
                    }).build());

    public VerticalSlabs(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    /** All registered vertical slab blocks. */
    public static List<Block> allSlabs() {
        return SLABS.stream().map(DeferredBlock::get).toList();
    }
}