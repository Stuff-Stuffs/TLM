package io.github.stuff_stuffs.tlm.common.block.entity;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorApi;
import io.github.stuff_stuffs.tlm.common.block.TLMBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public final class TLMBlockEntities {
    public static final BlockEntityType<ConveyorBlockEntity> CONVEYOR_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(ConveyorBlockEntity::new, TLMBlocks.CONVEYOR_BLOCK).build();

    public static void init() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, TLM.createId("conveyor"), CONVEYOR_BLOCK_ENTITY_TYPE);
        ConveyorApi.CONVEYOR_ACCESS_BLOCK_API_LOOKUP.registerForBlockEntity((blockEntity, unused) -> blockEntity.getConveyorAccess(), CONVEYOR_BLOCK_ENTITY_TYPE);
        ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP.registerForBlockEntity(ConveyorSupplier::getConveyor, CONVEYOR_BLOCK_ENTITY_TYPE);
    }

    private TLMBlockEntities() {
    }
}
