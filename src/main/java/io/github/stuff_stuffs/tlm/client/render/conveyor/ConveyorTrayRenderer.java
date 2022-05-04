package io.github.stuff_stuffs.tlm.client.render.conveyor;

import io.github.stuff_stuffs.tlm.client.TLMClient;
import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResource;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public final class ConveyorTrayRenderer {
    public static final EntityModelLayer CONVEYOR_TRAY_LAYER = new EntityModelLayer(TLM.createId("conveyor/conveyor_tray"), "TLM:ConveyorTray");
    public static final Identifier STACK_STATE_TEXTURE = TLM.createId("textures/conveyor/stack_marker.png");

    private ConveyorTrayRenderer() {
    }

    public static void render(final ConveyorTray tray, final ModelPart trayModel, final MatrixStack matrices, final int light, final VertexConsumerProvider vertexConsumers) {
        final Optional<ConveyedResource<?, ?>> optional = tray.getResource();
        if (optional.isPresent()) {
            final ConveyedResource<?, ?> resource = optional.get();
            renderResource(resource, matrices, light, vertexConsumers);
        }
        final VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(TLM.createId("textures/conveyor/conveyor_tray.png")));
        trayModel.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV);
        final ConveyorTrayDataStack firstStack = tray.getStack(0);
        if (!firstStack.isEmpty()) {
            matrices.push();
            matrices.translate(3 / 16.0F, 1 / 16.0F + 0.001F, -6 / 16.0F);
            matrices.scale(3 / 16.0F, 3 / 16.0F, 3 / 16.0F);
            renderStack(firstStack, matrices, light, vertexConsumers);
            matrices.pop();
        }
        final ConveyorTrayDataStack secondStack = tray.getStack(1);
        if (!secondStack.isEmpty()) {
            matrices.push();
            matrices.translate(-1 / 16.0F, 1 / 16.0F + 0.001F, -6 / 16.0F);
            matrices.scale(3 / 16.0F, 3 / 16.0F, 3 / 16.0F);
            renderStack(secondStack, matrices, light, vertexConsumers);
            matrices.pop();
        }
    }

    private static void renderStack(final ConveyorTrayDataStack stack, final MatrixStack matrices, final int light, final VertexConsumerProvider vertexConsumers) {
        final ConveyorTrayDataStack.State state = stack.peek();
        final VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(STACK_STATE_TEXTURE));
        final Matrix4f posMat = matrices.peek().getPositionMatrix();
        final Matrix3f normMat = matrices.peek().getNormalMatrix();
        buffer.vertex(posMat, 0, 0, 0).color(state.color).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normMat, 0, 1, 0).next();
        buffer.vertex(posMat, 0, 0, 1).color(state.color).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normMat, 0, 1, 0).next();
        buffer.vertex(posMat, 1, 0, 1).color(state.color).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normMat, 0, 1, 0).next();
        buffer.vertex(posMat, 1, 0, 0).color(state.color).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normMat, 0, 1, 0).next();
    }

    private static <O, T extends TransferVariant<O>> void renderResource(final ConveyedResource<O, T> resource, final MatrixStack matrices, final int light, final VertexConsumerProvider vertexConsumers) {
        TLMClient.getInfo(resource.getType()).renderer.render(resource, matrices, light, vertexConsumers);
    }

    public static TexturedModelData createTrayModel() {
        final ModelData data = new ModelData();
        final ModelPartData partData = data.getRoot();
        partData.addChild("base", ModelPartBuilder.create().uv(0, 0).cuboid(-6, 0, -6, 12, 1, 12, Dilation.NONE, 1.0F, 1.0F), ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 64);
    }
}
