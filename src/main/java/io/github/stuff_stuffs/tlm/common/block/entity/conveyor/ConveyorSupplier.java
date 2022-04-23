package io.github.stuff_stuffs.tlm.common.block.entity.conveyor;

import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorAccess;
import net.minecraft.util.math.Direction;

public interface ConveyorSupplier {
    ConveyorAccess getConveyorAccess();

    Conveyor getConveyor(Direction side);
}
