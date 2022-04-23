package io.github.stuff_stuffs.tlm.common.block.storage;

import io.github.stuff_stuffs.tlm.common.block.BlockEntityBlock;
import io.github.stuff_stuffs.tlm.common.block.entity.storage.StorageCrateBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;

import java.util.List;

public class StorageCrateBlock extends BlockEntityBlock<StorageCrateBlockEntity> {
    public StorageCrateBlock(final Settings settings) {
        super(settings, StorageCrateBlockEntity::new);
    }

    @Override
    public BlockEntityType<StorageCrateBlockEntity> getType() {
        return TLMBlockEntities.STORAGE_CRATE_BLOCK_ENTITY_TYPE;
    }
}
