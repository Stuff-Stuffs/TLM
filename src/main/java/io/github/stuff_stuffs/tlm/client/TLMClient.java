package io.github.stuff_stuffs.tlm.client;

import io.github.stuff_stuffs.tlm.client.render.DirectionalPlacingRenderer;
import io.github.stuff_stuffs.tlm.client.render.block.entity.ConveyorBlockEntityRenderer;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.item.TLMItem;
import io.github.stuff_stuffs.tlm.mixin.api.ClientWorldCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TLMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> ((ClientWorldCache) world).tlm_invalidateCache(blockEntity.getPos()));
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> ((ClientWorldCache) world).tlm_invalidateCache(blockEntity.getPos()));
        BlockEntityRendererRegistry.register(TLMBlockEntities.CONVEYOR_BLOCK_ENTITY_TYPE, ctx -> new ConveyorBlockEntityRenderer());
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(new WorldRenderEvents.BeforeBlockOutline() {
            @Override
            public boolean beforeBlockOutline(WorldRenderContext context, @Nullable HitResult hitResult) {
                final ItemStack activeItem = MinecraftClient.getInstance().player.getActiveItem();
                if(context.blockOutlines() && hitResult instanceof BlockHitResult blockHit && activeItem.getItem() instanceof TLMItem item && item.hasDirectionalPlacing()) {
                    DirectionalPlacingRenderer.render(context, blockHit);
                    return false;
                }
                return true;
            }
        });
    }
}
