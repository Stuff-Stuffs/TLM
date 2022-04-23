package io.github.stuff_stuffs.tlm.common.block.entity;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorApi;
import io.github.stuff_stuffs.tlm.common.api.storage.StorageApi;
import io.github.stuff_stuffs.tlm.common.block.TLMBlocks;
import io.github.stuff_stuffs.tlm.common.block.entity.conveyor.ConveyorBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.entity.conveyor.ConveyorSupplier;
import io.github.stuff_stuffs.tlm.common.block.entity.conveyor.LabelerBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.entity.storage.StorageCrateBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.entity.storage.StorageCrateLoaderBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.entity.storage.StorageCrateUnloaderBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public final class TLMBlockEntities {
    public static final BlockEntityType<ConveyorBlockEntity> CONVEYOR_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(ConveyorBlockEntity::new, TLMBlocks.CONVEYOR_BLOCK).build();
    public static final BlockEntityType<StorageCrateBlockEntity> STORAGE_CRATE_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(StorageCrateBlockEntity::new, TLMBlocks.STORAGE_CRATE_BLOCK).build();
    public static final BlockEntityType<StorageCrateUnloaderBlockEntity> STORAGE_CRATE_UNLOADER_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(StorageCrateUnloaderBlockEntity::new, TLMBlocks.STORAGE_CRATE_UNLOADER_BLOCK).build();
    public static final BlockEntityType<StorageCrateLoaderBlockEntity> STORAGE_CRATE_LOADER_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(StorageCrateLoaderBlockEntity::new, TLMBlocks.STORAGE_CRATE_LOADER_BLOCK).build();

    public static final BlockEntityType<LabelerBlockEntity> LABELER_BLOCK_ENTITY_BLOCK_TYPE = FabricBlockEntityTypeBuilder.create(LabelerBlockEntity::new, TLMBlocks.LABELER_BLOCK).build();

    public static void init() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, TLM.createId("conveyor"), CONVEYOR_BLOCK_ENTITY_TYPE);
        ConveyorApi.CONVEYOR_ACCESS_BLOCK_API_LOOKUP.registerForBlockEntity((blockEntity, unused) -> blockEntity.getConveyorAccess(), CONVEYOR_BLOCK_ENTITY_TYPE);
        ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP.registerForBlockEntity(ConveyorSupplier::getConveyor, CONVEYOR_BLOCK_ENTITY_TYPE);
        ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP.registerForBlockEntity(ConveyorSupplier::getConveyor, CONVEYOR_BLOCK_ENTITY_TYPE);

        Registry.register(Registry.BLOCK_ENTITY_TYPE, TLM.createId("storage_crate"), STORAGE_CRATE_BLOCK_ENTITY_TYPE);
        StorageApi.STORAGE_CRATE_BLOCK_API_LOOKUP.registerForBlockEntity((blockEntity, unused) -> blockEntity, STORAGE_CRATE_BLOCK_ENTITY_TYPE);

        Registry.register(Registry.BLOCK_ENTITY_TYPE, TLM.createId("storage_crate_unloader"), STORAGE_CRATE_UNLOADER_BLOCK_ENTITY_TYPE);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, TLM.createId("storage_crate_loader"), STORAGE_CRATE_LOADER_BLOCK_ENTITY_TYPE);

        Registry.register(Registry.BLOCK_ENTITY_TYPE, TLM.createId("labeler"), LABELER_BLOCK_ENTITY_BLOCK_TYPE);
        ConveyorApi.CONVEYOR_ACCESS_BLOCK_API_LOOKUP.registerForBlockEntity((blockEntity, unused) -> blockEntity.getConveyorAccess(), LABELER_BLOCK_ENTITY_BLOCK_TYPE);
        ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP.registerForBlockEntity(ConveyorSupplier::getConveyor, LABELER_BLOCK_ENTITY_BLOCK_TYPE);
        ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP.registerForBlockEntity(ConveyorSupplier::getConveyor, LABELER_BLOCK_ENTITY_BLOCK_TYPE);
    }

    private TLMBlockEntities() {
    }
}
