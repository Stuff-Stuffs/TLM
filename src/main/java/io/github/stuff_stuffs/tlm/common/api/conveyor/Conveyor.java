package io.github.stuff_stuffs.tlm.common.api.conveyor;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;

public interface Conveyor extends ConveyorAccess, ConveyorLike {
    boolean tryInsert(ConveyorTray tray, float tickUsed, long tickOrder);
}
