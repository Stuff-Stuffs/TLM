package io.github.stuff_stuffs.tlm.common.api.item;

public interface TLMItem {
    double DIRECTIONAL_PLACING_EDGE_THICKNESS = 0.125;

    boolean hasDirectionalPlacing();

    boolean hasGhostPlacing();
}
