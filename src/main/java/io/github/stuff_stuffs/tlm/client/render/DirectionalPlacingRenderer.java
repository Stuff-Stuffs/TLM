package io.github.stuff_stuffs.tlm.client.render;

import io.github.stuff_stuffs.tlm.common.api.item.TLMItem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;

public final class DirectionalPlacingRenderer {
    public static boolean render(final WorldRenderContext context, final WorldRenderContext.BlockOutlineContext outlineContext) {
        final BlockPos hitPos = outlineContext.blockPos();

        final MatrixStack matrices = context.matrixStack();
        final Vec3d camera = new Vec3d(outlineContext.cameraX(), outlineContext.cameraY(), outlineContext.cameraZ());
        final Vec3f look = new Vec3f(0, 0, 1);
        look.transform(new Matrix3f(context.camera().getRotation()));
        final Vec3d end = camera.add(new Vec3d(look).multiply(4));
        final BlockHitResult raycast = outlineContext.blockState().getOutlineShape(context.world(), hitPos).raycast(camera, end, hitPos);
        if (raycast != null && raycast.getType() != HitResult.Type.MISS) {
            final float edgeThickness = (float) TLMItem.DIRECTIONAL_PLACING_EDGE_THICKNESS;
            final float centerRad = 0.5F - edgeThickness;
            matrices.push();
            matrices.translate(hitPos.getX() - outlineContext.cameraX() + 0.5, hitPos.getY() - outlineContext.cameraY() + 0.5, hitPos.getZ() - outlineContext.cameraZ() + 0.5);
            matrices.multiply(raycast.getSide().getRotationQuaternion());
            matrices.translate(-0.5, 0.5, -0.5);
            final VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());
            renderSquare(matrices, 0, 0, 1, 1, consumer);
            renderSquare(matrices, 0.5F - centerRad, 0.5F - centerRad, 2 * centerRad, 2 * centerRad, consumer);

            final Matrix4f posMat = matrices.peek().getPositionMatrix();
            final Matrix3f normMat = matrices.peek().getNormalMatrix();
            consumer.vertex(posMat, 0, 0, 0).color(0, 0, 0, 1F).normal(normMat, 1, 0, 0).next();
            consumer.vertex(posMat, edgeThickness, 0, edgeThickness).color(0, 0, 0, 1F).normal(normMat, 1, 0, 0).next();

            consumer.vertex(posMat, 1, 0, 0).color(0, 0, 0, 0.4F).normal(normMat, 1, 0, 0).next();
            consumer.vertex(posMat, 1.0F - edgeThickness, 0, edgeThickness).color(0, 0, 0, 1F).normal(normMat, 1, 0, 0).next();

            consumer.vertex(posMat, 1, 0, 1).color(0, 0, 0, 1F).normal(normMat, 1, 0, 0).next();
            consumer.vertex(posMat, 1.0F - edgeThickness, 0, 1.0F - edgeThickness).color(0, 0, 0, 1F).normal(normMat, 1, 0, 0).next();

            consumer.vertex(posMat, 0, 0, 1).color(0, 0, 0, 0.4F).normal(normMat, 1, 0, 0).next();
            consumer.vertex(posMat, edgeThickness, 0, 1.0F - edgeThickness).color(0, 0, 0, 0.4F).normal(normMat, 1, 0, 0).next();
            matrices.pop();
            return true;
        }
        return false;
    }

    private static void renderSquare(final MatrixStack matrices, final float x, final float y, final float width, final float height, final VertexConsumer consumer) {
        final Matrix4f posMat = matrices.peek().getPositionMatrix();
        final Matrix3f normMat = matrices.peek().getNormalMatrix();
        consumer.vertex(posMat, x, 0, y).color(0, 0, 0, 0.4F).normal(normMat, 1, 0, 0).next();
        consumer.vertex(posMat, x + width, 0, y).color(0, 0, 0, 0.4F).normal(normMat, 1, 0, 0).next();

        consumer.vertex(posMat, x + width, 0, y).color(0, 0, 0, 0.4F).normal(normMat, 0, 0, 1).next();
        consumer.vertex(posMat, x + width, 0, y + height).color(0, 0, 0, 0.4F).normal(normMat, 0, 0, 1).next();

        consumer.vertex(posMat, x + width, 0, y + height).color(0, 0, 0, 0.4F).normal(normMat, -1, 0, 0).next();
        consumer.vertex(posMat, x, 0, y + height).color(0, 0, 0, 0.4F).normal(normMat, -1, 0, 0).next();

        consumer.vertex(posMat, x, 0, y + height).color(0, 0, 0, 0.4F).normal(normMat, 0, 0, -1).next();
        consumer.vertex(posMat, x, 0, y).color(0, 0, 0, 0.4F).normal(normMat, 0, 0, -1).next();
    }

    private DirectionalPlacingRenderer() {
    }
}
