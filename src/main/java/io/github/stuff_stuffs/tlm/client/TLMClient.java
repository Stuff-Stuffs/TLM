package io.github.stuff_stuffs.tlm.client;

import io.github.stuff_stuffs.tlm.client.network.UpdatingBlockEntityReceiver;
import io.github.stuff_stuffs.tlm.client.render.BlockGhostRenderer;
import io.github.stuff_stuffs.tlm.client.render.ConveyedResourceHud;
import io.github.stuff_stuffs.tlm.client.render.DirectionalPlacingRenderer;
import io.github.stuff_stuffs.tlm.client.render.block.entity.ConveyorSupplierBlockEntityRenderer;
import io.github.stuff_stuffs.tlm.client.render.block.model.UnbakedConveyorBlockModel;
import io.github.stuff_stuffs.tlm.client.render.block.model.UnbakedLabelerBlockModel;
import io.github.stuff_stuffs.tlm.client.render.block.model.UnbakedThreeSplitterConveyorBlockModel;
import io.github.stuff_stuffs.tlm.client.render.block.model.UnbakedTwoSplitterConveyorBlockModel;
import io.github.stuff_stuffs.tlm.client.render.conveyor.ConveyorTrayRenderer;
import io.github.stuff_stuffs.tlm.client.screen.LabelerBlockHandledScreen;
import io.github.stuff_stuffs.tlm.client.screen.ThreeSplitterBlockHandledScreen;
import io.github.stuff_stuffs.tlm.client.screen.TwoSplitterBlockHandledScreen;
import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.item.TLMItem;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResource;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.screen.TLMScreenHandlerTypes;
import io.github.stuff_stuffs.tlm.mixin.api.ClientWorldCache;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.client.texture.SpriteRegistryCallbackHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3f;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class TLMClient implements ClientModInitializer {
    private static final Map<ConveyedResourceType<?, ?>, ClientConveyedResourceInfo<?, ?>> INFOS = new Reference2ReferenceOpenHashMap<>();

    @Override
    public void onInitializeClient() {
        HandledScreens.register(TLMScreenHandlerTypes.LABELER_BLOCK_CONFIGURATION_SCREEN_HANDLER_TYPE, LabelerBlockHandledScreen::new);
        HandledScreens.register(TLMScreenHandlerTypes.TWO_SPLITTER_BLOCK_SCREEN_HANDLER_TYPE, TwoSplitterBlockHandledScreen::new);
        HandledScreens.register(TLMScreenHandlerTypes.THREE_SPLITTER_BLOCK_SCREEN_HANDLER_TYPE, ThreeSplitterBlockHandledScreen::new);
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> ((ClientWorldCache) world).tlm_invalidateCache(blockEntity.getPos()));
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> ((ClientWorldCache) world).tlm_invalidateCache(blockEntity.getPos()));
        BlockEntityRendererRegistry.register(TLMBlockEntities.CONVEYOR_BLOCK_ENTITY_TYPE, ConveyorSupplierBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(TLMBlockEntities.LABELER_BLOCK_ENTITY_BLOCK_TYPE, ConveyorSupplierBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(TLMBlockEntities.TWO_SPLITTER_CONVEYOR_BLOCK_ENTITY_TYPE, ConveyorSupplierBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(TLMBlockEntities.THREE_SPLITTER_CONVEYOR_BLOCK_ENTITY_TYPE, ConveyorSupplierBlockEntityRenderer::new);
        WorldRenderEvents.BLOCK_OUTLINE.register((context, hitResult) -> {
            if (MinecraftClient.getInstance().player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof TLMItem item && item.hasDirectionalPlacing()) {
                return !DirectionalPlacingRenderer.render(context, hitResult);
            }
            return true;
        });
        SpriteRegistryCallbackHolder.eventLocal(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register(new ClientSpriteRegistryCallback() {
            @Override
            public void registerSprites(final SpriteAtlasTexture atlasTexture, final Registry registry) {
                registry.register(TLM.createId("conveyor/stack_marker"));
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(BlockGhostRenderer::render);
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(manager -> (resourceId, context) -> {
            final String namespace = resourceId.getNamespace();
            if (TLM.MOD_ID.equals(namespace)) {
                final String path = resourceId.getPath();
                if ("block/conveyor".equals(path)) {
                    return new UnbakedConveyorBlockModel();
                }
                if ("block/labeler".equals(path)) {
                    return new UnbakedLabelerBlockModel();
                }
                if ("block/two_way_splitter_conveyor".equals(path)) {
                    return new UnbakedTwoSplitterConveyorBlockModel();
                }
                if ("block/three_way_splitter_conveyor".equals(path)) {
                    return new UnbakedThreeSplitterConveyorBlockModel();
                }
            }
            return null;
        });
        EntityModelLayerRegistry.registerModelLayer(ConveyorTrayRenderer.CONVEYOR_TRAY_LAYER, ConveyorTrayRenderer::createTrayModel);
        UpdatingBlockEntityReceiver.init();
        HudRenderCallback.EVENT.register(ConveyedResourceHud::render);
        addConveyedResourceInfo(ConveyedResourceType.CONVEYED_ITEM_TYPE, (tray, matrices, tickDelta) -> {
            final Optional<ConveyedResource<Item, ItemVariant>> cast = ConveyedResourceType.CONVEYED_ITEM_TYPE.cast(tray.getResource().get());
            if (cast.isEmpty()) {
                return;
            }
            final Transaction transaction = Transaction.openOuter();
            final ConveyedResource<Item, ItemVariant> resource = cast.get();
            int count = 0;
            for (final StorageView<ItemVariant> view : resource.getBackingStorage().iterable(transaction)) {
                count = (int) view.getAmount();
                break;
            }
            transaction.close();
            final Window window = MinecraftClient.getInstance().getWindow();
            MinecraftClient.getInstance().textRenderer.draw(matrices, "" + count, window.getScaledWidth() / 2.0F - 8.0F, window.getScaledHeight() / 2.0F, -1);
        }, (resource, matrices, light, vertexConsumers) -> {
            final Transaction transaction = Transaction.openOuter();
            final Storage<ItemVariant> storage = resource.getBackingStorage();
            final Iterator<? extends StorageView<ItemVariant>> views = storage.iterator(transaction);
            if (views.hasNext()) {
                final StorageView<ItemVariant> next = views.next();
                final ItemVariant variant = next.getResource();
                if (!variant.isBlank()) {
                    final MinecraftClient client = MinecraftClient.getInstance();
                    final ItemRenderer itemRenderer = client.getItemRenderer();
                    final ItemStack stack = variant.toStack();
                    final BakedModel model = itemRenderer.getModel(stack, client.world, null, 42);
                    if (model.hasDepth()) {
                        matrices.push();
                        matrices.translate(3.0 / 32.0, -1 / 16.0, 3.0 / 32.0);
                        matrices.scale(2.25F, 2.25F, 2.25F);
                        itemRenderer.renderItem(stack, ModelTransformation.Mode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 42);
                        matrices.pop();
                    } else {
                        matrices.push();
                        matrices.translate(2 / 16.0, 3 / 32.0, 0);
                        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
                        itemRenderer.renderItem(stack, ModelTransformation.Mode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 42);
                        matrices.pop();
                    }
                }
            }
            transaction.close();
        });
    }

    public static <O, T extends TransferVariant<O>> void addConveyedResourceInfo(final ConveyedResourceType<O, T> type, final ConveyedResourceHudRenderer hudRenderer, final ConveyedResourceRenderer<O, T> renderer) {
        if (INFOS.put(type, new ClientConveyedResourceInfo<>(hudRenderer, renderer)) != null) {
            throw new RuntimeException("Cannot have duplicate ClientConveyedResourceInfos");
        }
    }

    public static <O, T extends TransferVariant<O>> ClientConveyedResourceInfo<O, T> getInfo(final ConveyedResourceType<O, T> type) {
        final ClientConveyedResourceInfo<?, ?> info = INFOS.get(type);
        if (info == null) {
            throw new RuntimeException("Missing ClientConveyedResourceInfo");
        }
        return (ClientConveyedResourceInfo<O, T>) info;
    }

    public static final class ClientConveyedResourceInfo<O, T extends TransferVariant<O>> {
        public final ConveyedResourceHudRenderer hudRenderer;
        public final ConveyedResourceRenderer<O, T> renderer;

        private ClientConveyedResourceInfo(final ConveyedResourceHudRenderer hudRenderer, final ConveyedResourceRenderer<O, T> renderer) {
            this.hudRenderer = hudRenderer;
            this.renderer = renderer;
        }
    }

    public interface ConveyedResourceHudRenderer {
        void render(ConveyorTray tray, MatrixStack matrices, float tickDelta);
    }

    public interface ConveyedResourceRenderer<O, T extends TransferVariant<O>> {
        void render(ConveyedResource<O, T> resource, MatrixStack matrices, final int light, final VertexConsumerProvider vertexConsumers);
    }
}
