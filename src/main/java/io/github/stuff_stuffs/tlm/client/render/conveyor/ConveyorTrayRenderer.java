package io.github.stuff_stuffs.tlm.client.render.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorTray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public final class ConveyorTrayRenderer {
    private ConveyorTrayRenderer() {
    }

    public static void render(final ConveyorTray tray, final MatrixStack matrices, final int light, final VertexConsumerProvider vertexConsumers) {
        final float rad = ConveyorTray.TRAY_SIZE / 2.0F;
        final VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getSolid());
        final MatrixStack.Entry entry = matrices.peek();
        final Matrix4f posMat = entry.getPositionMatrix();
        final Matrix3f normMat = entry.getNormalMatrix();
        final Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(TLM.createId("tray"));
        buffer.vertex(posMat, -rad, 0, -rad).color(0xFFFFFFFF).texture(sprite.getMinU(), sprite.getMinV()).light(light).normal(normMat, 0, 1, 0).next();
        buffer.vertex(posMat, -rad, 0, rad).color(0xFFFFFFFF).texture(sprite.getMinU(), sprite.getMaxV()).light(light).normal(normMat, 0, 1, 0).next();
        buffer.vertex(posMat, rad, 0, rad).color(0xFFFFFFFF).texture(sprite.getMaxU(), sprite.getMaxV()).light(light).normal(normMat, 0, 1, 0).next();
        buffer.vertex(posMat, rad, 0, -rad).color(0xFFFFFFFF).texture(sprite.getMaxU(), sprite.getMinV()).light(light).normal(normMat, 0, 1, 0).next();
    }
}
