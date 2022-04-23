package io.github.stuff_stuffs.tlm.common.api.conveyor;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;

public interface ConveyorLike {
    float getMaximumOverlap();

    float getOverlapping();

    float getMinY(ConveyorTray tray, float overlap);
}
