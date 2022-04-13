package io.github.stuff_stuffs.tlm.common.block;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;

public final class TLMBlocks {
    public static final ConveyorBlock CONVEYOR_BLOCK = new ConveyorBlock(FabricBlockSettings.copyOf(Blocks.IRON_TRAPDOOR).hardness(1.0F).strength(1.0F));

    public static void init() {
        Registry.register(Registry.BLOCK, TLM.createId("conveyor"), CONVEYOR_BLOCK);
        TLMBlockEntities.init();
    }

    private TLMBlocks() {
    }
}
