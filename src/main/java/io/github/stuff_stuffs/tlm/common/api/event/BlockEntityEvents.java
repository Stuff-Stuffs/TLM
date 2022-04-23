package io.github.stuff_stuffs.tlm.common.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.BlockEntity;

public final class BlockEntityEvents {
    public static final Event<Remove> REMOVE = EventFactory.createArrayBacked(Remove.class, listeners -> blockEntity -> {
        for (Remove listener : listeners) {
            listener.onRemove(blockEntity);
        }
    });

    private BlockEntityEvents() {
    }

    public interface Remove {
        void onRemove(BlockEntity blockEntity);
    }
}
