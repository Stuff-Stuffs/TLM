package io.github.stuff_stuffs.tlm.common.item;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.block.TLMBlocks;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;

public final class TLMItems {
    public static final BlockItem CONVEYOR_BLOCK_ITEM = new TLMBlockItem(TLMBlocks.CONVEYOR_BLOCK, new FabricItemSettings(), true, true);

    public static void init() {
        Registry.register(Registry.ITEM, TLM.createId("conveyor"), CONVEYOR_BLOCK_ITEM);
    }

    private TLMItems() {
    }
}
