package io.github.stuff_stuffs.tlm.common.api.conveyor.impls;

import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorAccess;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

public abstract class DelegatingConveyor implements ConveyorAccess {
    private final ConveyorAccess accessDelegate;
    private final Function<@Nullable Direction, Conveyor> conveyorFactory;

    public DelegatingConveyor(final ConveyorAccess accessDelegate, final Function<@Nullable Direction, Conveyor> conveyorFactory) {
        this.accessDelegate = accessDelegate;
        this.conveyorFactory = conveyorFactory;
    }

    @Override
    public Iterator<ConveyorTray> getTrays() {
        return accessDelegate.getTrays();
    }

    public Conveyor getConveyor(@Nullable final Direction side) {
        return wrap(conveyorFactory.apply(side));
    }

    protected Conveyor wrap(final Conveyor conveyor) {
        return new BasicDelegate(conveyor);
    }

    protected static class BasicDelegate implements Conveyor {
        protected final Conveyor delegate;

        public BasicDelegate(final Conveyor delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean tryInsert(final ConveyorTray tray, final float tickUsed) {
            return delegate.tryInsert(tray, tickUsed);
        }

        @Override
        public Iterator<ConveyorTray> getTrays() {
            return delegate.getTrays();
        }

        @Override
        public float getMaximumOverlap() {
            return delegate.getMaximumOverlap();
        }

        @Override
        public float getOverlapping() {
            return delegate.getOverlapping();
        }

        @Override
        public float getMinY(final ConveyorTray tray, final float overlap) {
            return delegate.getMinY(tray, overlap);
        }
    }
}
