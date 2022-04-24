package io.github.stuff_stuffs.tlm.common.api.conveyor.impls;

import com.google.common.base.Preconditions;
import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorAccess;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorLike;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.util.MathUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.github.stuff_stuffs.tlm.common.api.conveyor.impls.AbstractSyncingConveyor.fromByte;

public class TwoSplitterConveyor implements ConveyorAccess {
    private static final Comparator<AbstractConveyor.Entry> COMPARATOR = Comparator.comparingDouble(entry -> entry.pos);
    private final List<AbstractConveyor.Entry> inEntries = new ArrayList<>();
    private final List<AbstractConveyor.Entry> out0Entries = new ArrayList<>();
    private final List<AbstractConveyor.Entry> out1Entries = new ArrayList<>();
    private final Map<Direction, @Nullable Conveyor> conveyorCache;
    private final float speed;
    private final Direction inSide;
    private final Direction out0Side;
    private final Direction out1Side;
    private final Vec3d start;
    private final Vec3d center;
    private final Vec3d startDelta;
    private final float startLength;
    private final Vec3d end0Delta;
    private final float end0Length;
    private final Vec3d end1Delta;
    private final float end1Length;
    private final Cache cache = new Cache();
    private Decider decider;
    private boolean syncNeeded = false;
    private Supplier<ConveyorLike> inputConveyorLikeCache;
    private Supplier<ConveyorLike> output0ConveyorLikeCache;
    private Supplier<Conveyor> output0ConveyorCache;
    private Supplier<ConveyorLike> output1ConveyorLikeCache;
    private Supplier<Conveyor> output1ConveyorCache;

    public TwoSplitterConveyor(final float speed, final Direction facing, final BlockPos pos, final Decider decider) {
        this.speed = speed;
        this.decider = decider;
        Preconditions.checkArgument(facing.getAxis() != Direction.Axis.Y);
        inSide = facing;
        out0Side = inSide.rotateYClockwise();
        out1Side = inSide.rotateYCounterclockwise();
        center = Vec3d.ofCenter(pos).add(0, -4 / 12.0, 0);
        start = center.withBias(inSide, 0.5);
        startDelta = center.subtract(start);
        startLength = 0.5F;
        end0Delta = center.withBias(out0Side, -0.5).subtract(center);
        end0Length = 0.5F;
        end1Delta = center.withBias(out1Side, -0.5).subtract(center);
        end1Length = 0.5F;
        conveyorCache = new EnumMap<>(Direction.class);
    }

    public void setup(final Supplier<ConveyorLike> inputConveyorLikeCache, final Supplier<ConveyorLike> output0ConveyorLikeCache, final Supplier<Conveyor> output0ConveyorCache, final Supplier<ConveyorLike> output1ConveyorLikeCache, final Supplier<Conveyor> output1ConveyorCache) {
        this.inputConveyorLikeCache = inputConveyorLikeCache;
        this.output0ConveyorLikeCache = output0ConveyorLikeCache;
        this.output0ConveyorCache = output0ConveyorCache;
        this.output1ConveyorLikeCache = output1ConveyorLikeCache;
        this.output1ConveyorCache = output1ConveyorCache;
    }

    public void setDecider(final Decider decider) {
        this.decider = decider;
    }

    public Conveyor getConveyor(final Direction side) {
        return conveyorCache.computeIfAbsent(side, this::computeConveyor);
    }

    private boolean tryInsert(final Direction side, final ConveyorTray tray, final float tickUsed) {
        if (side != inSide && side != null) {
            return false;
        }
        final AbstractConveyor.InsertMode insertMode = side == null ? AbstractConveyor.InsertMode.ANY_OVERRIDE : AbstractConveyor.InsertMode.START;
        updateCache();
        final float minPos = computeMinPos(Branch.NONE);
        if (insertMode == AbstractConveyor.InsertMode.START) {
            if (minPos > ConveyorTray.TRAY_SIZE / 2.0F) {
                return false;
            }
            final float maxPos = Math.min(computeMaxPos(Branch.NONE), getLastPos(Branch.NONE));
            if (!MathUtil.greaterThan(maxPos, minPos)) {
                return false;
            }
            final AbstractConveyor.Entry entry = new AbstractConveyor.Entry(tray, minPos, 1 - tickUsed);
            final int index = AbstractConveyor.getInsertIndex(inEntries, entry, COMPARATOR);
            inEntries.add(index, entry);
            updatePosition(Branch.NONE, entry, false);
        } else {
            final float maxPos = Math.min(computeMaxPos(Branch.NONE), getLastPos(Branch.NONE));
            if (!MathUtil.greaterThan(maxPos, minPos) && !MathUtil.equalTo(maxPos, minPos)) {
                return false;
            }
            final AbstractConveyor.Entry entry = new AbstractConveyor.Entry(tray, minPos, 1 - tickUsed);
            final int index = AbstractConveyor.getInsertIndex(inEntries, entry, COMPARATOR);
            inEntries.add(index, entry);
            updatePosition(Branch.NONE, entry, true);
        }
        syncNeeded = true;
        return true;
    }

    private void updateCache() {
        if (inputConveyorLikeCache != null) {
            cache.in = inputConveyorLikeCache.get();
        }
        if (output0ConveyorCache != null) {
            final Conveyor output = output0ConveyorCache.get();
            cache.output0 = output;
            if (output != null) {
                cache.out0 = output;
            } else if (output0ConveyorLikeCache != null) {
                cache.out0 = output0ConveyorLikeCache.get();
            }
        } else if (output0ConveyorLikeCache != null) {
            cache.out0 = output0ConveyorLikeCache.get();
        }
        if (output1ConveyorCache != null) {
            final Conveyor output = output1ConveyorCache.get();
            cache.output1 = output;
            if (output != null) {
                cache.out1 = output;
            } else if (output1ConveyorLikeCache != null) {
                cache.out1 = output1ConveyorLikeCache.get();
            }
        } else if (output0ConveyorLikeCache != null) {
            cache.out0 = output0ConveyorLikeCache.get();
        }
    }

    private float computeMinPos(final Branch branch) {
        if (branch == Branch.NONE) {
            if (cache.in == null) {
                return ConveyorTray.TRAY_SIZE / 2.0F;
            }
            final float maximumOverlap = cache.in.getMaximumOverlap();
            final float max = Math.max(cache.in.getOverlapping(), ConveyorTray.TRAY_SIZE / 2.0F);
            return max - maximumOverlap;
        } else {
            if (inEntries.isEmpty()) {
                return startLength;
            }
            final AbstractConveyor.Entry entry = inEntries.get(0);
            if (entry.pos + ConveyorTray.TRAY_SIZE > startLength) {
                //TODO maybe TRAY_SIZE?
                return startLength + ConveyorTray.TRAY_SIZE;
            }
            return startLength;
        }
    }

    private float computeMaxPos(final Branch branch) {
        if (branch == Branch.NONE) {
            final float max = startLength;
            if (out0Entries.isEmpty() && out1Entries.isEmpty()) {
                return max;
            }
            if (!out0Entries.isEmpty()) {
                final AbstractConveyor.Entry entry = out0Entries.get(out0Entries.size() - 1);
                if (entry.pos - ConveyorTray.TRAY_SIZE < startLength) {
                    return startLength - ConveyorTray.TRAY_SIZE;
                }
            }
            if (!out1Entries.isEmpty()) {
                final AbstractConveyor.Entry entry = out1Entries.get(out1Entries.size() - 1);
                if (entry.pos - ConveyorTray.TRAY_SIZE < startLength) {
                    return startLength - ConveyorTray.TRAY_SIZE;
                }
            }
            return max;
        } else if (branch == Branch.LEFT) {
            float max = startLength + end0Length - ConveyorTray.TRAY_SIZE / 2.0F;
            if (cache.out0 == null) {
                return max;
            }
            max += Math.min(ConveyorTray.TRAY_SIZE / 2.0F, cache.out0.getMaximumOverlap());
            max -= cache.out0.getOverlapping();
            return max;
        } else {
            float max = startLength + end1Length - ConveyorTray.TRAY_SIZE / 2.0F;
            if (cache.out1 == null) {
                return max;
            }
            max += Math.min(ConveyorTray.TRAY_SIZE / 2.0F, cache.out1.getMaximumOverlap());
            max -= cache.out1.getOverlapping();
            return max;
        }
    }

    private float getLastPos(final Branch branch) {
        if (branch == Branch.NONE) {
            return inEntries.isEmpty() ? Float.POSITIVE_INFINITY : inEntries.get(inEntries.size() - 1).getPos() - ConveyorTray.TRAY_SIZE;
        }
        if (branch == Branch.LEFT) {
            return out0Entries.isEmpty() ? Float.POSITIVE_INFINITY : out0Entries.get(out0Entries.size() - 1).getPos() - ConveyorTray.TRAY_SIZE;
        }
        return out1Entries.isEmpty() ? Float.POSITIVE_INFINITY : out1Entries.get(out1Entries.size() - 1).getPos() - ConveyorTray.TRAY_SIZE;
    }

    private float computeMaxOverlap(final Direction side) {
        if(inEntries.isEmpty()&&out0Entries.isEmpty()&&out1Entries.isEmpty()) {
            return ConveyorTray.TRAY_SIZE/2.0F;
        }
        final Direction.Axis axis = side.getAxis();
        if (side.getDirection() == Direction.AxisDirection.POSITIVE) {
            final Iterator<AbstractConveyor.Entry> trays = Stream.concat(Stream.concat(inEntries.stream(), out0Entries.stream()), out1Entries.stream()).iterator();
            float p = Float.NEGATIVE_INFINITY;
            while (trays.hasNext()) {
                final AbstractConveyor.Entry next = trays.next();
                final Box bounds = next.tray.getBounds(1);
                if (bounds.offset(center.multiply(-1)).contains(0, 0, 0)) {
                    final Vec3d center = bounds.getCenter();
                    p = Math.max(p, (float) axis.choose(center.x, center.y, center.z));
                }
            }
            return (float) Math.max(Math.abs(p - (0.5 - ConveyorTray.TRAY_SIZE/2.0F) - axis.choose(center.x, center.y, center.z)),0);
        } else {
            final Iterator<AbstractConveyor.Entry> trays = Stream.concat(Stream.concat(inEntries.stream(), out0Entries.stream()), out1Entries.stream()).iterator();
            float p = Float.POSITIVE_INFINITY;
            while (trays.hasNext()) {
                final AbstractConveyor.Entry next = trays.next();
                final Box bounds = next.tray.getBounds(1);
                if (bounds.offset(center.multiply(-1)).contains(0, 0, 0)) {
                    final Vec3d center = bounds.getCenter();
                    p = Math.min(p, (float) axis.choose(center.x, center.y, center.z));
                }
            }
            return (float) Math.max(Math.abs(p - (0.5 - ConveyorTray.TRAY_SIZE/2.0F) - axis.choose(center.x, center.y, center.z)),0);
        }
    }

    private float computeOverlap(final Direction side) {
        if(inEntries.isEmpty()&&out0Entries.isEmpty()&&out1Entries.isEmpty()) {
            return 0;
        }
        final Direction.Axis axis = side.getAxis();
        if (side.getDirection() == Direction.AxisDirection.POSITIVE) {
            final Iterator<AbstractConveyor.Entry> trays = Stream.concat(Stream.concat(inEntries.stream(), out0Entries.stream()), out1Entries.stream()).iterator();
            float p = Float.NEGATIVE_INFINITY;
            while (trays.hasNext()) {
                final AbstractConveyor.Entry next = trays.next();
                final Box bounds = next.tray.getBounds(1);
                if (bounds.offset(center.multiply(-1)).contains(0, 0, 0)) {
                    final Vec3d center = bounds.getCenter();
                    p = Math.max(p, (float) axis.choose(center.x, center.y, center.z));
                }
            }
            return (float) Math.max(Math.abs(p - axis.choose(center.x, center.y, center.z)) - 0.5,0);
        } else {
            final Iterator<AbstractConveyor.Entry> trays = Stream.concat(Stream.concat(inEntries.stream(), out0Entries.stream()), out1Entries.stream()).iterator();
            float p = Float.POSITIVE_INFINITY;
            while (trays.hasNext()) {
                final AbstractConveyor.Entry next = trays.next();
                final Box bounds = next.tray.getBounds(1);
                if (bounds.offset(center.multiply(-1)).contains(0, 0, 0)) {
                    final Vec3d center = bounds.getCenter();
                    p = Math.min(p, (float) axis.choose(center.x, center.y, center.z));
                }
            }
            return (float) Math.max(Math.abs(p - axis.choose(center.x, center.y, center.z)) - 0.5,0);
        }
    }

    private float computeMinY(final @Nullable Direction side, final float overlap) {
        return (float) center.y;
    }

    private @Nullable Conveyor computeConveyor(final Direction side) {
        if (side == inSide || side == out0Side || side == out1Side) {
            return new Conveyor() {
                @Override
                public boolean tryInsert(final ConveyorTray tray, final float tickUsed) {
                    return TwoSplitterConveyor.this.tryInsert(side, tray, tickUsed);
                }

                @Override
                public Iterator<ConveyorTray> getTrays() {
                    return TwoSplitterConveyor.this.getTrays();
                }

                @Override
                public float getMaximumOverlap() {
                    return computeMaxOverlap(side);
                }

                @Override
                public float getOverlapping() {
                    return computeOverlap(side);
                }

                @Override
                public float getMinY(final ConveyorTray tray, final float overlap) {
                    return computeMinY(side, overlap);
                }
            };
        }
        if (side == null) {
            return new Conveyor() {
                @Override
                public boolean tryInsert(final ConveyorTray tray, final float tickUsed) {
                    return TwoSplitterConveyor.this.tryInsert(null, tray, tickUsed);
                }

                @Override
                public Iterator<ConveyorTray> getTrays() {
                    return TwoSplitterConveyor.this.getTrays();
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
                    return computeMinY(null, overlap);
                }
            };
        }
        return null;
    }

    public void tick() {
        updateCache();
        tick(Branch.NONE, inEntries);
        tick(Branch.LEFT, out0Entries);
        tick(Branch.RIGHT, out1Entries);
    }

    private void tick(final Branch branch, final List<AbstractConveyor.Entry> entries) {
        for (final AbstractConveyor.Entry entry : entries) {
            if (entry.tickRemaining == -1.0F) {
                entry.tickRemaining = 1.0F;
            }
        }
        final float maxPos = computeMaxPos(branch);
        int i = 0;
        int entryCount = entries.size();
        while (i < entryCount) {
            final AbstractConveyor.Entry entry = entries.get(i);
            final float movement = entry.tickRemaining * speed;
            float nextPos = movement + entry.pos;
            if (i > 0) {
                nextPos = Math.min(nextPos, entries.get(i - 1).pos - ConveyorTray.TRAY_SIZE);
            }
            boolean skip = false;
            if (MathUtil.greaterThan(nextPos, maxPos)) {
                final float tickUsed = (nextPos - maxPos) / movement;
                entries.remove(i);
                if (tryAdvance(branch, entry, tickUsed)) {
                    syncNeeded = true;
                    skip = true;
                } else {
                    entries.add(i, entry);
                    nextPos = maxPos;
                }
            }
            if (skip) {
                entryCount = entryCount - 1;
            } else {
                entry.pos = nextPos;
                updatePosition(branch, entry, false);
                entry.tickRemaining = -1;
                i++;
            }
        }
    }

    private boolean tryAdvance(final Branch branch, final AbstractConveyor.Entry entry, final float tickUsed) {
        if (branch == Branch.NONE) {
            final Dir dir = decider.decide(entry.tray);
            if (dir == Dir.LEFT) {
                final float minPos = computeMinPos(Branch.LEFT);
                if (minPos-startLength > ConveyorTray.TRAY_SIZE / 2.0F) {
                    return false;
                }
                final float maxPos = Math.min(computeMaxPos(Branch.LEFT), getLastPos(Branch.LEFT));
                if (!MathUtil.greaterThan(maxPos, minPos)) {
                    return false;
                }
                entry.tickRemaining = 1 - tickUsed;
                final int index = AbstractConveyor.getInsertIndex(out0Entries, entry, COMPARATOR);
                out0Entries.add(index, entry);
                updatePosition(Branch.LEFT, entry, false);
            } else {
                final float minPos = computeMinPos(Branch.RIGHT);
                if (minPos-startLength > ConveyorTray.TRAY_SIZE / 2.0F) {
                    return false;
                }
                final float maxPos = Math.min(computeMaxPos(Branch.RIGHT), getLastPos(Branch.RIGHT));
                if (!MathUtil.greaterThan(maxPos, minPos)) {
                    return false;
                }
                entry.tickRemaining = 1 - tickUsed;
                final int index = AbstractConveyor.getInsertIndex(out1Entries, entry, COMPARATOR);
                out1Entries.add(index, entry);
                updatePosition(Branch.RIGHT, entry, false);
            }
            decider.onAccept(entry.tray);
            return true;
        } else if (branch == Branch.LEFT) {
            if (cache.output0 == null) {
                return false;
            }
            return cache.output0.tryInsert(entry.getTray(), tickUsed);
        } else {
            if (cache.output1 == null) {
                return false;
            }
            return cache.output1.tryInsert(entry.getTray(), tickUsed);
        }
    }

    private void updatePosition(final Branch branch, final AbstractConveyor.Entry entry, final boolean override) {
        if (branch == Branch.NONE) {
            entry.getTray().setPosition(start.add(startDelta.multiply(entry.pos / startLength)), override);
        } else if (branch == Branch.LEFT) {
            entry.getTray().setPosition(center.add(end0Delta.multiply((entry.pos - startLength) / end0Length)), override);
        } else {
            entry.getTray().setPosition(center.add(end1Delta.multiply((entry.pos - startLength) / end1Length)), override);
        }
    }

    @Override
    public Iterator<ConveyorTray> getTrays() {
        return Stream.concat(inEntries.stream(), Stream.concat(out0Entries.stream(), out1Entries.stream())).map(AbstractConveyor.Entry::getTray).iterator();
    }

    public boolean isSyncNeeded() {
        if (syncNeeded) {
            return true;
        } else {
            final Iterator<ConveyorTray> trays = getTrays();
            while (trays.hasNext()) {
                if (trays.next().isSyncNeeded()) {
                    return true;
                }
            }
            return false;
        }
    }

    public void readSyncFromBuf(final PacketByteBuf buf) {
        inEntries.clear();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            final ConveyorTray tray = ConveyorTray.readFromNbt(buf.readNbt(), true);
            final float pos = buf.readFloat();
            final float tickRemaining = fromByte(buf.readByte());
            final AbstractConveyor.Entry e = new AbstractConveyor.Entry(tray, pos, tickRemaining);
            inEntries.add(e);
            updatePosition(Branch.NONE, e, true);
        }
        out0Entries.clear();
        count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            final ConveyorTray tray = ConveyorTray.readFromNbt(buf.readNbt(), true);
            final float pos = buf.readFloat();
            final float tickRemaining = fromByte(buf.readByte());
            final AbstractConveyor.Entry e = new AbstractConveyor.Entry(tray, pos, tickRemaining);
            out0Entries.add(e);
            updatePosition(Branch.LEFT, e, true);
        }
        out1Entries.clear();
        count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            final ConveyorTray tray = ConveyorTray.readFromNbt(buf.readNbt(), true);
            final float pos = buf.readFloat();
            final float tickRemaining = fromByte(buf.readByte());
            final AbstractConveyor.Entry e = new AbstractConveyor.Entry(tray, pos, tickRemaining);
            out1Entries.add(e);
            updatePosition(Branch.RIGHT, e, true);
        }
    }

    public void writeSyncToBuf(final PacketByteBuf buf) {
        buf.writeVarInt(inEntries.size());
        for (final AbstractConveyor.Entry entry : inEntries) {
            buf.writeNbt(entry.getTray().writeToNbt(true));
            buf.writeFloat(entry.getPos());
            buf.writeByte(AbstractSyncingConveyor.toByte(entry.getTickRemaining()));
        }
        buf.writeVarInt(out0Entries.size());
        for (final AbstractConveyor.Entry entry : out0Entries) {
            buf.writeNbt(entry.getTray().writeToNbt(true));
            buf.writeFloat(entry.getPos());
            buf.writeByte(AbstractSyncingConveyor.toByte(entry.getTickRemaining()));
        }
        buf.writeVarInt(out1Entries.size());
        for (final AbstractConveyor.Entry entry : out1Entries) {
            buf.writeNbt(entry.getTray().writeToNbt(true));
            buf.writeFloat(entry.getPos());
            buf.writeByte(AbstractSyncingConveyor.toByte(entry.getTickRemaining()));
        }
    }

    public void clearSyncFlag() {
        syncNeeded = false;
        final Iterator<ConveyorTray> trays = getTrays();
        while (trays.hasNext()) {
            trays.next().clearSyncFlag();
        }
    }

    private enum Branch {
        NONE,
        LEFT,
        RIGHT
    }

    private static final class Cache {
        private ConveyorLike in;
        private ConveyorLike out0;
        private Conveyor output0;
        private ConveyorLike out1;
        private Conveyor output1;
    }

    public interface Decider {
        Dir decide(ConveyorTray tray);

        void onAccept(ConveyorTray tray);
    }

    public enum Dir {
        LEFT,
        RIGHT
    }
}
