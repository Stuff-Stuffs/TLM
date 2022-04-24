package io.github.stuff_stuffs.tlm.common.api.conveyor.impls;

import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorAccess;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorLike;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.util.MathUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractConveyor implements ConveyorAccess {
    private static final Comparator<Entry> COMPARATOR = Comparator.comparingDouble(entry -> entry.pos);
    private final Conveyor nullSidedConveyor;
    private final EnumMap<Direction, Conveyor> conveyorCache;
    protected final List<Entry> entries = new ArrayList<>();
    protected final float speed;
    protected boolean syncNeeded = true;

    protected AbstractConveyor(final float speed) {
        nullSidedConveyor = computeConveyor(null);
        conveyorCache = new EnumMap<>(Direction.class);
        this.speed = speed;
    }

    public void clearSyncFlag() {
        syncNeeded = false;
        for (final Entry entry : entries) {
            entry.getTray().clearSyncFlag();
        }
    }

    public boolean isSyncNeeded() {
        if (syncNeeded) {
            return true;
        }
        for (final Entry entry : entries) {
            if (entry.getTray().isSyncNeeded()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<ConveyorTray> getTrays() {
        return entries.stream().map(Entry::getTray).iterator();
    }

    public abstract void writeSyncToBuf(PacketByteBuf buf);

    public abstract void readSyncFromBuf(PacketByteBuf buf);

    protected abstract void updateCache();

    protected abstract float computeMinPos();

    protected abstract float computeMaxPos();

    protected abstract boolean tryAdvance(Entry entry, float tickUsed);

    protected abstract void updatePosition(Entry entry, boolean override);

    protected abstract InsertMode getInsertMode(ConveyorTray tray, @Nullable Direction direction);

    public @Nullable Conveyor getConveyor(@Nullable final Direction side) {
        if (side == null) {
            return nullSidedConveyor;
        }
        return conveyorCache.computeIfAbsent(side, this::computeConveyor);
    }

    protected abstract @Nullable Conveyor computeConveyor(@Nullable final Direction side);

    protected boolean tryInsert(final ConveyorTray tray, @Nullable final Direction direction, final float usedTick) {
        final InsertMode mode = getInsertMode(tray, direction);
        return switch (mode) {
            case NONE -> false;
            case START, START_OVERRIDE -> {
                updateCache();
                final float minPos = Math.max(computeMinPos(), -ConveyorTray.TRAY_SIZE / 2.0F);
                if (minPos > ConveyorTray.TRAY_SIZE / 2.0F) {
                    yield false;
                }
                final float maxPos = Math.min(computeMaxPos(), getLastPos());
                if (!MathUtil.greaterThan(maxPos, minPos)) {
                    yield false;
                }
                syncNeeded = true;
                final Entry entry = new Entry(tray, minPos, 1 - usedTick);
                final int index = getInsertIndex(entries, entry, COMPARATOR);
                entries.add(index, entry);
                updatePosition(entry, mode == InsertMode.START_OVERRIDE);
                yield true;
            }
            case ANY_OVERRIDE -> {
                updateCache();
                final float minPos = computeMinPos();
                final float maxPos = Math.min(computeMaxPos(), getLastPos());
                if (!MathUtil.greaterThan(maxPos, minPos) && !MathUtil.equalTo(maxPos, minPos)) {
                    yield false;
                }
                final Entry entry = new Entry(tray, minPos, 1 - usedTick);
                final int index = getInsertIndex(entries, entry, COMPARATOR);
                entries.add(index, entry);
                updatePosition(entry, true);
                syncNeeded = true;
                yield true;
            }
        };
    }

    protected float getLastPos() {
        return entries.isEmpty() ? Float.POSITIVE_INFINITY : entries.get(entries.size() - 1).pos - ConveyorTray.TRAY_SIZE;
    }

    public abstract void setup(Supplier<ConveyorLike> inputConveyorLikeCache, Supplier<ConveyorLike> outputConveyorLikeCache, Supplier<Conveyor> outputConveyorCache);

    public static <T> int getInsertIndex(final List<? extends T> list, final T toInsert, final Comparator<? super T> comparator) {
        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final T midVal = list.get(mid);
            final int comp = comparator.compare(midVal, toInsert);
            if (comp > 0) {
                low = mid + 1;
            } else if (comp < 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return low;
    }

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

    protected static final class Entry {
        final ConveyorTray tray;
        float pos;
        float tickRemaining;

        public Entry(final ConveyorTray tray, final float pos, final float tickRemaining) {
            this.tray = tray;
            this.pos = pos;
            this.tickRemaining = tickRemaining;
        }

        public ConveyorTray getTray() {
            return tray;
        }

        public float getPos() {
            return pos;
        }

        public float getTickRemaining() {
            return tickRemaining;
        }
    }

    public enum InsertMode {
        NONE,
        ANY_OVERRIDE,
        START,
        START_OVERRIDE
    }
}
