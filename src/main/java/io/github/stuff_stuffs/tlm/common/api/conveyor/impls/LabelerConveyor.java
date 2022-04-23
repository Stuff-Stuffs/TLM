package io.github.stuff_stuffs.tlm.common.api.conveyor.impls;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
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
    public void tick() {
        updateCache();
        for (final Entry entry : entries) {
            if (entry.tickRemaining == -1.0F) {
                entry.tickRemaining = 1.0F;
            }
        }
        final float maxPos = computeMaxPos();
        int i = 0;
        int entryCount = entries.size();
        while (i < entryCount) {
            final Entry entry = entries.get(i);
            final float movement = entry.tickRemaining * speed;
            float nextPos = movement + entry.pos;
            if (i > 0) {
                nextPos = Math.min(nextPos, entries.get(i - 1).pos - ConveyorTray.TRAY_SIZE);
            }
            boolean skip = false;
            if (MathUtil.greaterThan(nextPos, maxPos)) {
                final float tickUsed = (nextPos - maxPos) / movement;
                entries.remove(i);
                if (tryAdvance(entry, tickUsed)) {
                    syncNeeded = true;
                    skip = true;
                } else {
                    entries.add(i, entry);
                    nextPos = maxPos;
                }
            }
            if (!MathUtil.greaterThan(entry.pos, length * 0.5F) && (skip || MathUtil.greaterThan(nextPos, 0.5F * length))) {
                final ConveyorTrayDataStack stack = entry.getTray().getStack(stackIndex);
                if (!stack.isFull()) {
                    stack.push(label);
                }
            }
            if (skip) {
                entryCount = entryCount - 1;
            } else {
                entry.pos = nextPos;
                updatePosition(entry, false);
                entry.tickRemaining = -1;
                i++;
            }
        }
    }
}
