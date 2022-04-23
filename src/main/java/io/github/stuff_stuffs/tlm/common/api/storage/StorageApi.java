package io.github.stuff_stuffs.tlm.common.api.storage;

import io.github.stuff_stuffs.tlm.common.TLM;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;

public final class StorageApi {
    public static final BlockApiLookup<StorageCrate, Void> STORAGE_CRATE_BLOCK_API_LOOKUP = BlockApiLookup.get(TLM.createId("storage_crate"), StorageCrate.class, Void.class);

    private StorageApi() {
    }
}
