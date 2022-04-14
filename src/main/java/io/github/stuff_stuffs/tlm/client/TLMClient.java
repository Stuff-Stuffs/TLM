package io.github.stuff_stuffs.tlm.client;

import io.github.stuff_stuffs.tlm.client.render.block.UnbakedConveyorBlockModel;
import io.github.stuff_stuffs.tlm.client.render.block.entity.ConveyorBlockEntityRenderer;
import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.mixin.api.ClientWorldCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.texture.SpriteAtlasTexture;

@Environment(EnvType.CLIENT)
public class TLMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> ((ClientWorldCache) world).tlm_invalidateCache(blockEntity.getPos()));
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> ((ClientWorldCache) world).tlm_invalidateCache(blockEntity.getPos()));
        BlockEntityRendererRegistry.register(TLMBlockEntities.CONVEYOR_BLOCK_ENTITY_TYPE, ctx -> new ConveyorBlockEntityRenderer());
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> registry.register(TLM.createId("block/conveyor_belt")));
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(manager -> (resourceId, context) -> {
            final String namespace = resourceId.getNamespace();
            if (TLM.MOD_ID.equals(namespace)) {
                final String path = resourceId.getPath();
                if ("block/conveyor".equals(path)) {
                    return new UnbakedConveyorBlockModel();
                }
            }
            return null;
        });
    }
}
