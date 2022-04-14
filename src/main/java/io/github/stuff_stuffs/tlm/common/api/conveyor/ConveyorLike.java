package io.github.stuff_stuffs.tlm.common.api.conveyor;

public interface ConveyorLike {
    float getMaximumOverlap();

    float getOverlapping();

    void updatePosition(ConveyorTray tray, float overlap);
}
