package io.github.stuff_stuffs.tlm.common.block;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.block.conveyor.ConveyorBlock;
import io.github.stuff_stuffs.tlm.common.block.conveyor.LabelerBlock;
import io.github.stuff_stuffs.tlm.common.block.conveyor.TwoSplitterConveyorBlock;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.storage.StorageCrateBlock;
import io.github.stuff_stuffs.tlm.common.block.storage.StorageCrateLoaderBlock;
import io.github.stuff_stuffs.tlm.common.block.storage.StorageCrateUnloaderBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;

public final class TLMBlocks {
    public static final ConveyorBlock CONVEYOR_BLOCK = new ConveyorBlock(FabricBlockSettings.copyOf(Blocks.IRON_TRAPDOOR).hardness(1.0F).strength(1.0F));
    public static final StorageCrateBlock STORAGE_CRATE_BLOCK = new StorageCrateBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS));
    public static final StorageCrateUnloaderBlock STORAGE_CRATE_UNLOADER_BLOCK = new StorageCrateUnloaderBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
    public static final StorageCrateLoaderBlock STORAGE_CRATE_LOADER_BLOCK = new StorageCrateLoaderBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
    public static final LabelerBlock LABELER_BLOCK = new LabelerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
    public static final TwoSplitterConveyorBlock TWO_SPLITTER_CONVEYOR_BLOCK = new TwoSplitterConveyorBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));

    public static void init() {
        Registry.register(Registry.BLOCK, TLM.createId("conveyor"), CONVEYOR_BLOCK);
        Registry.register(Registry.BLOCK, TLM.createId("storage_crate"), STORAGE_CRATE_BLOCK);
        Registry.register(Registry.BLOCK, TLM.createId("storage_crate_unloader"), STORAGE_CRATE_UNLOADER_BLOCK);
        Registry.register(Registry.BLOCK, TLM.createId("storage_crate_loader"), STORAGE_CRATE_LOADER_BLOCK);
        Registry.register(Registry.BLOCK, TLM.createId("labeler"), LABELER_BLOCK);
        Registry.register(Registry.BLOCK, TLM.createId("two_way_splitter_conveyor"), TWO_SPLITTER_CONVEYOR_BLOCK);
        TLMBlockEntities.init();
    }

    private TLMBlocks() {
    }
}
