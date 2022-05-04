package io.github.stuff_stuffs.tlm.common.api.conveyor.impls;

import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorLike;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class MultiSegmentConveyor extends AbstractSyncingConveyor {
    private final Direction insertSide;
    private final Direction outSide;
    private final List<LineSegment> segments;
    private final float length;
    private final Cache cache;
    private Supplier<@Nullable ConveyorLike> inGetter = () -> null;
    private Supplier<@Nullable ConveyorLike> outGetter = () -> null;
    private Supplier<@Nullable Conveyor> outputGetter = () -> null;

    public MultiSegmentConveyor(final float speed, final Direction insertSide, final Direction outSide, final List<Vec3d> segmentPoints) {
        super(speed);
        this.insertSide = insertSide;
        this.outSide = outSide;
        if (segmentPoints.size() < 2) {
            throw new RuntimeException("Multiple points required for a segment to form!");
        }
        segments = new ArrayList<>(segmentPoints.size() - 1);
        float sum = 0;
        for (int i = 0; i < segmentPoints.size() - 1; i++) {
            final Vec3d start = segmentPoints.get(i);
            final Vec3d end = segmentPoints.get(i + 1);
            final LineSegment segment = new LineSegment(start, end);
            segments.add(segment);
            sum += segment.length;
        }
        length = sum;
        cache = new Cache();
    }

    @Override
    public void setup(final Supplier<@Nullable ConveyorLike> inGetter, final Supplier<@Nullable ConveyorLike> outGetter, final Supplier<@Nullable Conveyor> outputGetter) {
        this.inGetter = inGetter;
        this.outGetter = outGetter;
        this.outputGetter = outputGetter;
    }

    @Override
    public void writeToNbt(final NbtCompound compound) {
        final NbtList list = new NbtList();
        for (final Entry entry : entries) {
            final NbtCompound nbt = new NbtCompound();
            nbt.put("data", entry.tray.writeToNbt(false));
            nbt.putFloat("pos", entry.pos);
            list.add(nbt);
        }
        compound.put("entries", list);
    }

    @Override
    public void readFromNbt(final NbtCompound compound) {
        entries.clear();
        final NbtList list = compound.getList("entries", NbtElement.COMPOUND_TYPE);
        for (final NbtElement element : list) {
            final NbtCompound nbt = (NbtCompound) element;
            final Entry entry = new Entry(ConveyorTray.readFromNbt(nbt.getCompound("data"), false), nbt.getFloat("pos"), 1);
            entries.add(getInsertIndex(entries, entry, COMPARATOR), entry);
        }
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
    protected boolean tryAdvance(final Entry entry, final float tickUsed, final long tickOrder) {
        if (cache.output == null) {
            return false;
        }
        return cache.output.tryInsert(entry.getTray(), tickUsed, tickOrder);
    }

    @Override
    protected void updatePosition(final Entry entry, final boolean override) {
        final float pos = entry.getPos();
        final ConveyorTray tray = entry.getTray();
        updatePosition(tray, pos, override);
    }

    private void updatePosition(final ConveyorTray tray, final float pos, final boolean override) {
        float sum = 0;
        for (final LineSegment segment : segments) {
            if (sum + segment.length >= pos) {
                Vec3d newPos = segment.start.add(segment.deltaNorm.multiply(pos - sum));
                double minY = getMinY(tray, pos);
                if (pos < ConveyorTray.TRAY_SIZE / 2.0F) {
                    if (cache.in != null) {
                        minY = Math.max(minY, cache.in.getMinY(tray, Math.abs(pos - ConveyorTray.TRAY_SIZE / 2.0F)));
                    }
                }
                if (pos > length - ConveyorTray.TRAY_SIZE / 2.0F) {
                    if (cache.out != null) {
                        minY = Math.max(minY, cache.out.getMinY(tray, pos + ConveyorTray.TRAY_SIZE / 2.0F - length));
                    }
                }
                minY = Math.max(minY, getMinY(tray, Math.max(0, pos - ConveyorTray.TRAY_SIZE / 2.0F)));
                minY = Math.max(minY, getMinY(tray, Math.min(length, pos + ConveyorTray.TRAY_SIZE / 2.0F)));
                newPos = newPos.withAxis(Direction.Axis.Y, minY);
                newPos = newPos.withAxis(Direction.Axis.Y, minY);
                tray.setPosition(newPos, override);
                break;
            } else {
                sum += segment.length;
            }
        }
    }

    private float getMinY(final ConveyorTray tray, final float pos) {
        float sum = 0;
        float minY = Float.NEGATIVE_INFINITY;
        for (final LineSegment segment : segments) {
            if (sum + segment.length > pos) {
                minY = Math.max(minY, (float) segment.start.y + (float) segment.deltaNorm.y * (pos - sum));
                break;
            } else {
                sum += segment.length;
            }
        }
        return minY;
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
        if (side == null) {
            return new Conveyor() {
                @Override
                public boolean tryInsert(final ConveyorTray tray, final float tickUsed, final long tickOrder) {
                    return MultiSegmentConveyor.this.tryInsert(tray, null, tickUsed, tickOrder);
                }

                @Override
                public Iterator<ConveyorTray> getTrays() {
                    return MultiSegmentConveyor.this.getTrays();
                }

                @Override
                public float getMaximumOverlap() {
                    return 0;
                }

                @Override
                public float getOverlapping() {
                    return 0;
                }

                @Override
                public float getMinY(final ConveyorTray tray, final float overlap) {
                    return MultiSegmentConveyor.this.getMinY(tray, length / 2.0F);
                }
            };
        }
        if (side == insertSide) {
            return new Conveyor() {
                @Override
                public boolean tryInsert(final ConveyorTray tray, final float tickUsed, final long tickOrder) {
                    return MultiSegmentConveyor.this.tryInsert(tray, insertSide, tickUsed, tickOrder);
                }

                @Override
                public Iterator<ConveyorTray> getTrays() {
                    return MultiSegmentConveyor.this.getTrays();
                }

                @Override
                public float getMaximumOverlap() {
                    return entries.isEmpty() ? ConveyorTray.TRAY_SIZE / 2.0F : Math.min(Math.max(entries.get(entries.size() - 1).getPos() - ConveyorTray.TRAY_SIZE / 2.0F, 0), ConveyorTray.TRAY_SIZE / 2.0F);
                }

                @Override
                public float getOverlapping() {
                    return entries.isEmpty() ? 0 : Math.max(-(entries.get(entries.size() - 1).getPos() - ConveyorTray.TRAY_SIZE / 2.0F), 0);
                }

                @Override
                public float getMinY(final ConveyorTray tray, final float overlap) {
                    return MultiSegmentConveyor.this.getMinY(tray, overlap);
                }
            };
        }
        if (side == outSide) {
            return new Conveyor() {
                @Override
                public boolean tryInsert(final ConveyorTray tray, final float tickUsed, final long tickOrder) {
                    return MultiSegmentConveyor.this.tryInsert(tray, outSide, tickUsed, tickOrder);
                }

                @Override
                public Iterator<ConveyorTray> getTrays() {
                    return MultiSegmentConveyor.this.getTrays();
                }

                @Override
                public float getMaximumOverlap() {
                    return entries.isEmpty() ? ConveyorTray.TRAY_SIZE / 2.0F : Math.min(Math.max(length - entries.get(0).getPos(), 0), ConveyorTray.TRAY_SIZE / 2.0F);
                }

                @Override
                public float getOverlapping() {
                    return entries.isEmpty() ? 0 : Math.max(entries.get(0).getPos() - length + ConveyorTray.TRAY_SIZE / 2.0F, 0);
                }

                @Override
                public float getMinY(final ConveyorTray tray, final float overlap) {
                    return Math.max(MultiSegmentConveyor.this.getMinY(tray, length - overlap), MultiSegmentConveyor.this.getMinY(tray, length));
                }
            };
        }
        return null;
    }

    private static final class LineSegment {
        private final Vec3d start;
        private final Vec3d deltaNorm;
        private final float length;

        private LineSegment(final Vec3d start, final Vec3d end) {
            this.start = start;
            final Vec3d delta = end.subtract(start);
            final double len = delta.length();
            deltaNorm = delta.multiply(1 / len);
            length = (float) len;
        }
    }

    private static final class Cache {
        private ConveyorLike in;
        private ConveyorLike out;
        private Conveyor output;
    }
}
