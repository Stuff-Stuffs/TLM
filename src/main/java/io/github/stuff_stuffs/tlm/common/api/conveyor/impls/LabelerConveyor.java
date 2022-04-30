package io.github.stuff_stuffs.tlm.common.api.conveyor.impls;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import io.github.stuff_stuffs.tlm.common.util.MathUtil;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class LabelerConveyor extends SimpleConveyor {
    private ConveyorTrayDataStack.State label;
    private int stackIndex;

    public LabelerConveyor(final float speed, final Direction insertSide, final Direction outSide, final Vec3d start, final Vec3d end, final ConveyorTrayDataStack.State label, final int stackIndex) {
        super(speed, insertSide, outSide, start, end);
        this.label = label;
        this.stackIndex = stackIndex;
    }

    public void setLabel(final ConveyorTrayDataStack.State label) {
        this.label = label;
    }

    public void setStackIndex(final int stackIndex) {
        this.stackIndex = stackIndex;
    }

    @Override
    protected boolean moveIteration(final float maxPos, final Entry entry, final int next, final long tickOrder) {
        if (!MathUtil.greaterThan(entry.pos, length * 0.5F)) {
            final boolean b = super.moveIteration(maxPos, entry, next, tickOrder);
            if (!b || MathUtil.greaterThan(entry.pos, 0.5F * length)) {
                final ConveyorTrayDataStack stack = entry.getTray().getStack(stackIndex);
                if (!stack.isFull()) {
                    stack.push(label);
                }
            }
            return b;
        }
        return super.moveIteration(maxPos, entry, next, tickOrder);
    }
}
