package io.github.stuff_stuffs.tlm.common.block;

import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import net.minecraft.state.property.EnumProperty;

public final class TLMBlockProperties {
    public static final EnumProperty<ConveyorOrientation> CONVEYOR_ORIENTATION_PROPERTY = EnumProperty.of("conveyor_orientation", ConveyorOrientation.class);
    public static final EnumProperty<ConveyorOrientation> CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY = EnumProperty.of("conveyor_straight_flat_orientation", ConveyorOrientation.class, orientation -> orientation.getType() == ConveyorOrientation.Type.STRAIGHT);

    private TLMBlockProperties() {
    }
}
