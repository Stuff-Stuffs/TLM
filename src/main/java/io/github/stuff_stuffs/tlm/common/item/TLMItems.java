package io.github.stuff_stuffs.tlm.common.item;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.block.TLMBlocks;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;

public final class TLMItems {
    public static final BlockItem CONVEYOR_BLOCK_ITEM = new TLMBlockItem(TLMBlocks.CONVEYOR_BLOCK, new FabricItemSettings(), true, true);
    public static final BlockItem STORAGE_ARRAY_CRATE_ITEM = new TLMBlockItem(TLMBlocks.STORAGE_CRATE_BLOCK, new FabricItemSettings(), false, false);
    public static final BlockItem STORAGE_CRATE_UNLOADER = new TLMBlockItem(TLMBlocks.STORAGE_CRATE_UNLOADER_BLOCK, new FabricItemSettings(), false, false);
    public static final BlockItem STORAGE_CRATE_LOADER = new TLMBlockItem(TLMBlocks.STORAGE_CRATE_LOADER_BLOCK, new FabricItemSettings(), false, false);
    public static final BlockItem LABELER_ITEM = new TLMBlockItem(TLMBlocks.LABELER_BLOCK, new FabricItemSettings(), false, true);
    public static final BlockItem TWO_SPLITTER_CONVEYOR_ITEM = new TLMBlockItem(TLMBlocks.TWO_SPLITTER_CONVEYOR_BLOCK, new FabricItemSettings(), false, true);
    public static final BlockItem THREE_SPLITTER_CONVEYOR_ITEM = new TLMBlockItem(TLMBlocks.THREE_SPLITTER_CONVEYOR_BLOCK, new FabricItemSettings(), false, true);

    public static void init() {
        Registry.register(Registry.ITEM, TLM.createId("conveyor"), CONVEYOR_BLOCK_ITEM);
        Registry.register(Registry.ITEM, TLM.createId("storage_crate"), STORAGE_ARRAY_CRATE_ITEM);
        Registry.register(Registry.ITEM, TLM.createId("storage_crate_unloader"), STORAGE_CRATE_UNLOADER);
        Registry.register(Registry.ITEM, TLM.createId("storage_crate_loader"), STORAGE_CRATE_LOADER);
        Registry.register(Registry.ITEM, TLM.createId("labeler"), LABELER_ITEM);
        Registry.register(Registry.ITEM, TLM.createId("two_way_splitter_conveyor"), TWO_SPLITTER_CONVEYOR_ITEM);
        Registry.register(Registry.ITEM, TLM.createId("three_way_splitter_conveyor"), THREE_SPLITTER_CONVEYOR_ITEM);
    }

    private TLMItems() {
    }
}
