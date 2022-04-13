package io.github.stuff_stuffs.tlm.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public final class DirectionalPlacingRenderer {
    private static final float SIDE_THICKNESS = 0.25F;

    public static void render(final WorldRenderContext context, final BlockHitResult hitResult) {
        final BlockPos hitPos = hitResult.getBlockPos();
        final VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getLines());
        final MatrixStack matrices = context.matrixStack();
        WorldRenderer.drawBox(matrices, buffer, hitPos.getX(), hitPos.getY(), hitPos.getZ(), hitPos.getX() + 1, hitPos.getY() + 1, hitPos.getZ() + 1, 1, 0, 0, 1);
    }

    private DirectionalPlacingRenderer() {
    }
}
