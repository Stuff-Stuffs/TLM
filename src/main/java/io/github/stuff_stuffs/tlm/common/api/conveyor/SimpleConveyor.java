package io.github.stuff_stuffs.tlm.common.api.conveyor;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Supplier;

public class SimpleConveyor extends AbstractConveyor {
    private final Direction insertSide;
    private final Direction outSide;
    private final Vec3d start;
    private final Vec3d normDelta;
    private final float length;
    private final Cache cache;
    private Supplier<@Nullable ConveyorLike> inGetter;
    private Supplier<@Nullable ConveyorLike> outGetter;
    private Supplier<@Nullable Conveyor> outputGetter;

    public SimpleConveyor(final float speed, final Direction insertSide, final Direction outSide, final Vec3d start, final Vec3d end) {
        super(speed);
        this.insertSide = insertSide;
        this.outSide = outSide;
        this.start = start;
        final Vec3d delta = end.subtract(start);
        final double len = delta.length();
        normDelta = delta.multiply(1 / len);
        length = (float) len;
        cache = new Cache();
        inGetter = () -> null;
        outGetter = () -> null;
        outputGetter = () -> null;
    }

    @Override
    public void setup(final Supplier<@Nullable ConveyorLike> inGetter, final Supplier<@Nullable ConveyorLike> outGetter, final Supplier<@Nullable Conveyor> outputGetter) {
        this.inGetter = inGetter;
        this.outGetter = outGetter;
        this.outputGetter = outputGetter;
    }

    @Override
    protected void updateCache() {
        cache.in = inGetter.get();
        final Conveyor output = outputGetter.get();
        cache.output = output;
        if (output != null) {
            cache.out = output;
        } else {
            cache.out = outGetter.get();
        }
    }

    @Override
    protected float computeMinPos() {
        if (cache.in == null) {
            return ConveyorTray.TRAY_SIZE / 2.0F;
        }
        final float maximumOverlap = cache.in.getMaximumOverlap();
        final float max = Math.max(cache.in.getOverlapping(), ConveyorTray.TRAY_SIZE / 2.0F);
        return max - maximumOverlap;
    }

    @Override
    protected float computeMaxPos() {
        float max = length - ConveyorTray.TRAY_SIZE / 2.0F;
        if (cache.out == null) {
            return max;
        }
        max += Math.min(ConveyorTray.TRAY_SIZE / 2.0F, cache.out.getMaximumOverlap());
        max -= cache.out.getOverlapping();
        return max;
    }

    @Override
    protected boolean tryAdvance(final AbstractConveyor.Entry entry, final float tickUsed) {
        if (cache.output == null) {
            return false;
        }
        return cache.output.tryInsert(entry.getTray(), tickUsed);
    }

    @Override
    protected void updatePosition(final AbstractConveyor.Entry entry, final boolean override) {
        final float pos = entry.getPos();
        entry.getTray().setPosition(start.add(normDelta.multiply(pos)), override);
    }

    @Override
    protected InsertMode getInsertMode(final ConveyorTray tray, @Nullable final Direction direction) {
        if (direction == null) {
            return InsertMode.ANY_OVERRIDE;
        }
        if (direction == insertSide) {
            return InsertMode.START;
        }
        return InsertMode.NONE;
    }

    @Override
    protected Conveyor computeConveyor(@Nullable final Direction side) {
        return new Conveyor() {
            @Override
            public boolean tryInsert(final ConveyorTray tray, final float tickUsed) {
                return SimpleConveyor.this.tryInsert(tray, side, tickUsed);
            }

            @Override
            public Iterator<ConveyorTray> getTrays() {
                return SimpleConveyor.this.getTrays();
            }

            @Override
            public float getMaximumOverlap() {
                if (side == outSide) {
                    return entries.isEmpty() ? ConveyorTray.TRAY_SIZE / 2.0F : Math.min(Math.max(length - entries.get(0).getPos(), 0), ConveyorTray.TRAY_SIZE / 2.0F);
                }
                if (side != insertSide) {
                    return 0;
                }
                return entries.isEmpty() ? ConveyorTray.TRAY_SIZE / 2.0F : Math.min(Math.max(entries.get(entries.size() - 1).getPos() - ConveyorTray.TRAY_SIZE / 2.0F, 0), ConveyorTray.TRAY_SIZE / 2.0F);
            }

            @Override
            public float getOverlapping() {
                if (side == insertSide) {
                    return entries.isEmpty() ? 0 : Math.max(-(entries.get(entries.size() - 1).getPos() - ConveyorTray.TRAY_SIZE / 2.0F), 0);
                }
                if (side != outSide) {
                    return 0;
                }
                return entries.isEmpty() ? 0 : Math.max(entries.get(0).getPos() - length + ConveyorTray.TRAY_SIZE / 2.0F, 0);
            }

            @Override
            public void updatePosition(final ConveyorTray tray, final float overlap) {
                tray.setPosition(start.add(normDelta.multiply(overlap)), false);
            }
        };
    }

    private static final class Cache {
        private ConveyorLike in;
        private ConveyorLike out;
        private Conveyor output;
    }
}
