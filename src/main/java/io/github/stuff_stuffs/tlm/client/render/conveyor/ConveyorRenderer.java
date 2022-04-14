package io.github.stuff_stuffs.tlm.client.render.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.block.properties.ConveyorOrientation;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Vec3f;

import java.util.function.Supplier;

public final class ConveyorRenderer {
    public static final Supplier<Sprite> SPRITE_SUPPLIER = () -> MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(TLM.createId("block/conveyor_belt"));

    public static void render(final float length, final float width, final ConveyorOrientation orientation, final RenderContext ctx) {
        final Direction startDir = orientation.getInputSide().getOpposite();
        final Matrix3f mat = new Matrix3f(startDir.getRotationQuaternion());
        final Vec3f scratch = new Vec3f();
        ctx.pushTransform(quad -> {
            for (int i = 0; i < 4; i++) {
                quad.copyPos(i, scratch);
                scratch.add(-0.5F, -0.5F, -0.5F);
                scratch.transform(mat);
                scratch.add(0.5F, 0.5F, 0.5F);
                quad.pos(i, scratch);
            }
            return true;
        });
        switch (orientation) {
            case NORTH, SOUTH, EAST, WEST -> renderStraight(length, width, ctx);
            case NORTH_TO_EAST, SOUTH_TO_WEST, EAST_TO_SOUTH, WEST_TO_NORTH -> renderCorner(length, width, true, ctx);
            case NORTH_TO_WEST, SOUTH_TO_EAST, EAST_TO_NORTH, WEST_TO_SOUTH -> renderCorner(length, width, false, ctx);
            case NORTH_UP, SOUTH_UP, EAST_UP, WEST_UP -> renderSlope(length, width, true, ctx);
            case NORTH_DOWN, SOUTH_DOWN, EAST_DOWN, WEST_DOWN -> renderSlope(length, width, false, ctx);
        }
        ctx.popTransform();
    }

    private static void renderStraight(final float length, final float width, final RenderContext ctx) {
        final QuadEmitter emitter = ctx.getEmitter();
        final int color = 0xFFFFFFFF;
        final float width2 = width / 2.0F;
        emitter.square(Direction.NORTH, 0.5F - width2, 0, 0.5F + width2, length, 15.0F / 16.0F);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, SPRITE_SUPPLIER.get(), MutableQuadView.BAKE_NORMALIZED);
        emitter.spriteColor(0, color, color, color, color);
        emitter.emit();

        emitter.square(Direction.SOUTH, 0.5F - width2, 0, 0.5F + width2, length, 0);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, SPRITE_SUPPLIER.get(), MutableQuadView.BAKE_NORMALIZED | MutableQuadView.BAKE_ROTATE_180);
        emitter.spriteColor(0, color, color, color, color);
        emitter.emit();
    }

    private static void renderCorner(final float length, final float width, final boolean clockwise, final RenderContext ctx) {

    }

    private static void renderSlope(final float length, final float width, final boolean up, final RenderContext ctx) {
        final QuadEmitter emitter = ctx.getEmitter();
        final int color = 0xFFFFFFFF;
        final float width2 = width / 2.0F;
        emitter.square(Direction.NORTH, 0.5F - width2, 0, 0.5F + width2, length, 15.0F / 16.0F);
        slopify(emitter, up);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, SPRITE_SUPPLIER.get(), MutableQuadView.BAKE_NORMALIZED);
        emitter.spriteColor(0, color, color, color, color);
        emitter.emit();

        emitter.square(Direction.SOUTH, 0.5F - width2, 0, 0.5F + width2, length, 0);
        slopify(emitter, up);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, SPRITE_SUPPLIER.get(), MutableQuadView.BAKE_NORMALIZED | MutableQuadView.BAKE_ROTATE_180);
        emitter.spriteColor(0, color, color, color, color);
        emitter.emit();
    }

    private static void slopify(QuadEmitter emitter, boolean up) {
        Vec3f scratch = new Vec3f();
        if (up) {
            for (int i = 0; i < 4; i++) {
                emitter.copyPos(i, scratch);
                if (scratch.getY() == 1) {
                    scratch.add(0, 0, -1);
                    emitter.pos(i, scratch);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                emitter.copyPos(i, scratch);
                if (scratch.getY() == 0) {
                    scratch.add(0, 0, -1);
                    emitter.pos(i, scratch);
                }
            }
        }
    }

    private ConveyorRenderer() {
    }
}
