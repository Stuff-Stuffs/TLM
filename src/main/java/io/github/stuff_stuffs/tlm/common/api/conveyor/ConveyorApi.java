package io.github.stuff_stuffs.tlm.common.api.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.impl.ConveyorItemStorageWrapper;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.util.math.Direction;

public final class ConveyorApi {
    public static final BlockApiLookup<ConveyorAccess, Void> CONVEYOR_ACCESS_BLOCK_API_LOOKUP = BlockApiLookup.get(TLM.createId("conveyor_access"), ConveyorAccess.class, Void.class);
    public static final BlockApiLookup<ConveyorLike, Direction> CONVEYOR_LIKE_BLOCK_API_LOOKUP = BlockApiLookup.get(TLM.createId("conveyor_like"), ConveyorLike.class, Direction.class);
    public static final BlockApiLookup<Conveyor, Direction> CONVEYOR_BLOCK_API_LOOKUP = BlockApiLookup.get(TLM.createId("conveyor"), Conveyor.class, Direction.class);

    public static void init() {
        ItemStorage.SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
            final Conveyor conveyor = CONVEYOR_BLOCK_API_LOOKUP.find(world, pos, state, blockEntity, context);
            if (conveyor != null) {
                return new ConveyorItemStorageWrapper(conveyor);
            } else {
                final ConveyorAccess access = CONVEYOR_ACCESS_BLOCK_API_LOOKUP.find(world, pos, state, blockEntity, null);
                if (access != null) {
                    return new ConveyorItemStorageWrapper(access);
                }
            }
            return null;
        });
    }

    private ConveyorApi() {
    }
}
