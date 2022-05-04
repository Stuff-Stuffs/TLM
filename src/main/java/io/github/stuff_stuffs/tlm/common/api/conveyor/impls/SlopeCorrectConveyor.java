package io.github.stuff_stuffs.tlm.common.api.conveyor.impls;

import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorLike;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.util.MathUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Supplier;

public class SlopeCorrectConveyor extends AbstractSyncingConveyor {
    private final Direction insertSide;
    private final Direction outSide;
    private final Vec3d start;
    private final Vec3d flat;
    private final float slope;
    private final float length;
    private final Cache cache;
    private Supplier<@Nullable ConveyorLike> inGetter = () -> null;
    private Supplier<@Nullable ConveyorLike> outGetter = () -> null;
    private Supplier<@Nullable Conveyor> outputGetter = () -> null;

    private SlopeCorrectConveyor(final float speed, final Direction insertSide, final Direction outSide, final Vec3d start, final Vec3d flat, final float slope, final float length, final float stretch) {
        super(speed * stretch);
        this.insertSide = insertSide;
        this.outSide = outSide;
        this.start = start;
        this.flat = flat;
        this.slope = slope;
        this.length = length;
        cache = new Cache();
    }

    private float getMinY(final ConveyorTray tray, final float pos) {
        return (float) start.y + slope * pos;
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
        if (inGetter != null) {
            cache.in = inGetter.get();
        }
        if (outputGetter != null) {
            final Conveyor output = outputGetter.get();
            cache.output = output;
            if (output != null) {
                cache.out = output;
            } else {
                cache.out = outGetter.get();
            }
        } else if (outGetter != null) {
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
        Vec3d newPos = start.add(flat.multiply(pos));
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
        entry.getTray().setPosition(newPos, override);
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
                    return SlopeCorrectConveyor.this.tryInsert(tray, null, tickUsed, tickOrder);
                }

                @Override
                public Iterator<ConveyorTray> getTrays() {
                    return SlopeCorrectConveyor.this.getTrays();
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
                    return SlopeCorrectConveyor.this.getMinY(tray, length / 2.0F);
                }
            };
        }
        if (side == insertSide) {
            return new Conveyor() {
                @Override
                public boolean tryInsert(final ConveyorTray tray, final float tickUsed, final long tickOrder) {
                    return SlopeCorrectConveyor.this.tryInsert(tray, insertSide, tickUsed, tickOrder);
                }

                @Override
                public Iterator<ConveyorTray> getTrays() {
                    return SlopeCorrectConveyor.this.getTrays();
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
                    return SlopeCorrectConveyor.this.getMinY(tray, overlap);
                }
            };
        }
        if (side == outSide) {
            return new Conveyor() {
                @Override
                public boolean tryInsert(final ConveyorTray tray, final float tickUsed, final long tickOrder) {
                    return SlopeCorrectConveyor.this.tryInsert(tray, outSide, tickUsed, tickOrder);
                }

                @Override
                public Iterator<ConveyorTray> getTrays() {
                    return SlopeCorrectConveyor.this.getTrays();
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
                    return Math.max(SlopeCorrectConveyor.this.getMinY(tray, length - overlap), SlopeCorrectConveyor.this.getMinY(tray, length));
                }
            };
        }
        return null;
    }

    public static AbstractConveyor create(final float speed, final Direction insertSide, final Direction outSide, final Vec3d start, final Vec3d end) {
        if (start.y == end.y) {
            return new SimpleConveyor(speed, insertSide, outSide, start, end);
        }
        final Vec3d delta = end.subtract(start);
        final double len = delta.length();
        final Vec3d deltaNorm = delta.multiply(1 / len);
        if (MathUtil.equalTo(Math.abs((float) deltaNorm.y - 1.0F), 0.0F)) {
            return new SimpleConveyor(speed, insertSide, outSide, start, end);
        }
        final Vec3d flat = delta.withAxis(Direction.Axis.Y, 0);
        return new SlopeCorrectConveyor(speed, insertSide, outSide, start, flat, (float) (delta.y / Math.hypot(delta.x, delta.z)), (float) flat.length(), (float) (len / flat.length()));
    }

    private static final class Cache {
        private ConveyorLike in;
        private ConveyorLike out;
        private Conveyor output;
    }
}
