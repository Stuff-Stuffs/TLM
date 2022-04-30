package io.github.stuff_stuffs.tlm.common;

import io.github.stuff_stuffs.tlm.common.api.UpdatingBlockEntity;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import io.github.stuff_stuffs.tlm.common.block.TLMBlocks;
import io.github.stuff_stuffs.tlm.common.item.TLMItems;
import io.github.stuff_stuffs.tlm.common.network.UpdatingBlockEntitySender;
import io.github.stuff_stuffs.tlm.common.screen.TLMScreenHandlerTypes;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class TLM implements ModInitializer {
    public static final String MOD_ID = "tlm";
    public static final Logger LOGGER = LoggerFactory.getLogger("TLM");
    private static final Set<UpdatingBlockEntity> UPDATING_BLOCK_ENTITIES = new ReferenceOpenHashSet<>();
    private static long tickOrder = Long.MIN_VALUE;

    @Override
    public void onInitialize() {
        ConveyedResourceType.init();
        TLMBlocks.init();
        TLMItems.init();
        TLMScreenHandlerTypes.init();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            UPDATING_BLOCK_ENTITIES.forEach(b -> b.update(buf -> UpdatingBlockEntitySender.send((BlockEntity) b, buf)));
            tickOrder++;
            UpdatingBlockEntitySender.tick();
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
            if (blockEntity instanceof UpdatingBlockEntity updating) {
                UPDATING_BLOCK_ENTITIES.add(updating);
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> UPDATING_BLOCK_ENTITIES.remove(blockEntity));
    }

    public static long getTickOrder() {
        return tickOrder;
    }

    public static Identifier createId(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
