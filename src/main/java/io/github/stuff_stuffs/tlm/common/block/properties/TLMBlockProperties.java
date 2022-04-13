package io.github.stuff_stuffs.tlm.common.block.properties;

import net.minecraft.state.property.EnumProperty;

public final class TLMBlockProperties {
    public static final EnumProperty<ConveyorOrientation> CONVEYOR_ORIENTATION_PROPERTY = EnumProperty.of("conveyor_orientation", ConveyorOrientation.class);

    private TLMBlockProperties() {
    }
}
