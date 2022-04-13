package io.github.stuff_stuffs.tlm.common.api.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.util.math.Direction;

public final class ConveyorApi {
    public static final BlockApiLookup<ConveyorAccess, Void> CONVEYOR_ACCESS_BLOCK_API_LOOKUP = BlockApiLookup.get(TLM.createId("conveyor_access"), ConveyorAccess.class, Void.class);
    public static final BlockApiLookup<ConveyorLike, Direction> CONVEYOR_LIKE_BLOCK_API_LOOKUP = BlockApiLookup.get(TLM.createId("conveyor_like"), ConveyorLike.class, Direction.class);
    public static final BlockApiLookup<Conveyor, Direction> CONVEYOR_BLOCK_API_LOOKUP = BlockApiLookup.get(TLM.createId("conveyor"), Conveyor.class, Direction.class);

    private ConveyorApi() {
    }
}
