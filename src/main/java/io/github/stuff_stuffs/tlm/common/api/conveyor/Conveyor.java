package io.github.stuff_stuffs.tlm.common.api.conveyor;

public interface Conveyor extends ConveyorAccess, ConveyorLike {
    boolean tryInsert(ConveyorTray tray, float tickUsed);
}
