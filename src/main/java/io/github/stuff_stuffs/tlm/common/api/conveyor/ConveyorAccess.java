package io.github.stuff_stuffs.tlm.common.api.conveyor;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;

import java.util.Iterator;

public interface ConveyorAccess {
    Iterator<ConveyorTray> getTrays();
}
