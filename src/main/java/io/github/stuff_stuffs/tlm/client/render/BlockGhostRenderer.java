package io.github.stuff_stuffs.tlm.client.render;

import io.github.stuff_stuffs.tlm.common.api.item.TLMItem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

public final class BlockGhostRenderer {
    public static void render(final WorldRenderContext context) {
        final ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();
        final Item item = stack.getItem();
        if (item instanceof BlockItem blockItem && item instanceof TLMItem tlmItem && tlmItem.hasGhostPlacing()) {
            final Vec3d cameraPos = context.camera().getPos();
            final Vec3f look = new Vec3f(0, 0, 1);
            look.transform(new Matrix3f(context.camera().getRotation()));
            final Vec3d end = cameraPos.add(new Vec3d(look).multiply(5));
            final BlockHitResult raycast = context.world().raycast(new RaycastContext(cameraPos, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, MinecraftClient.getInstance().cameraEntity));
            if (raycast != null && raycast.getType() != HitResult.Type.MISS) {
                BlockState posState = context.world().getBlockState(raycast.getBlockPos().offset(raycast.getSide()));
                final ItemPlacementContext itemContext = new ItemPlacementContext(MinecraftClient.getInstance().player, Hand.MAIN_HAND, stack, raycast);
                final ItemPlacementContext placementContext = blockItem.getPlacementContext(itemContext);
                if(!posState.canReplace(placementContext)) {
                    return;
                }
                final BlockState state = blockItem.getBlock().getPlacementState(placementContext);
                final BlockPos pos = raycast.getBlockPos().offset(raycast.getSide());
                final MatrixStack matrixStack = context.matrixStack();
                matrixStack.push();
                matrixStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
                final BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
                final BlockModelRenderer renderer = manager.getModelRenderer();
                final VertexConsumer ghosted = new GhostVertexConsumer(context.consumers().getBuffer(RenderLayer.getTranslucent()));
                renderer.render(context.world(), manager.getModel(state), state, pos, matrixStack, ghosted, false, context.world().getRandom(), 42, OverlayTexture.DEFAULT_UV);
                matrixStack.pop();
            }
        }
    }

    private static final class GhostVertexConsumer implements VertexConsumer {
        private static final float ALPHA_FACTOR = 0.75F;
        private final VertexConsumer wrapped;

        private GhostVertexConsumer(final VertexConsumer wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public VertexConsumer vertex(final double x, final double y, final double z) {
            return wrapped.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(final int red, final int green, final int blue, final int alpha) {
            return wrapped.color(red, green, blue, MathHelper.ceil(alpha * ALPHA_FACTOR));
        }

        @Override
        public VertexConsumer texture(final float u, final float v) {
            return wrapped.texture(u, v);
        }

        @Override
        public VertexConsumer overlay(final int u, final int v) {
            return wrapped.overlay(u, v);
        }

        @Override
        public VertexConsumer light(final int u, final int v) {
            return wrapped.light(u, v);
        }

        @Override
        public VertexConsumer normal(final float x, final float y, final float z) {
            return wrapped.normal(x, y, z);
        }

        @Override
        public void next() {
            wrapped.next();
        }

        @Override
        public void vertex(final float x, final float y, final float z, final float red, final float green, final float blue, float alpha, final float u, final float v, final int overlay, final int light, final float normalX, final float normalY, final float normalZ) {
            alpha = alpha * ALPHA_FACTOR;
            wrapped.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
        }

        @Override
        public void fixedColor(final int red, final int green, final int blue, final int alpha) {
            wrapped.fixedColor(red, green, blue, alpha);
        }

        @Override
        public void unfixColor() {
            wrapped.unfixColor();
        }

        @Override
        public VertexConsumer color(final float red, final float green, final float blue, final float alpha) {
            return wrapped.color(red, green, blue, alpha * ALPHA_FACTOR);
        }

        @Override
        public VertexConsumer color(int argb) {
            int a = (argb >>> 24) & 0xFF;
            a = MathHelper.ceil(a * ALPHA_FACTOR);
            argb = argb & 0xFFFFFF | a << 24;
            return wrapped.color(argb);
        }

        @Override
        public VertexConsumer light(final int uv) {
            return wrapped.light(uv);
        }

        @Override
        public VertexConsumer overlay(final int uv) {
            return wrapped.overlay(uv);
        }

        @Override
        public VertexConsumer vertex(final Matrix4f matrix, final float x, final float y, final float z) {
            return wrapped.vertex(matrix, x, y, z);
        }

        @Override
        public VertexConsumer normal(final Matrix3f matrix, final float x, final float y, final float z) {
            return wrapped.normal(matrix, x, y, z);
        }
    }

    private BlockGhostRenderer() {
    }
}
