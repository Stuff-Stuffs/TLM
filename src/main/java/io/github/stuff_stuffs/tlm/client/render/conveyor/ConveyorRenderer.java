package io.github.stuff_stuffs.tlm.client.render.conveyor;

import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ConveyorRenderer {
    private final Map<ConveyorOrientation, Mesh> meshes;
    private ConveyorOrientation cachedOrientation = null;
    private final Map<Direction, List<BakedQuad>> cachedQuads = new EnumMap<>(Direction.class);
    private List<BakedQuad> allCachedQuads = List.of();
    private final float length;
    private final float width;
    private final Sprite sprite;
    private final Sprite clockWiseSprite;
    private final Sprite counterClockWiseSprite;

    public Mesh getMesh(final ConveyorOrientation orientation) {
        return meshes.computeIfAbsent(orientation, this::compute);
    }

    private Mesh compute(final ConveyorOrientation orientation) {
        final MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        final Direction startDir = orientation.getInputSide().getOpposite();
        final Matrix3f mat = new Matrix3f(startDir.getRotationQuaternion());
        final QuadEmitter emitter = meshBuilder.getEmitter();
        switch (orientation) {
            case NORTH, SOUTH, EAST, WEST -> renderStraight(length, width, mat, emitter);
            case NORTH_TO_EAST, SOUTH_TO_WEST, EAST_TO_SOUTH, WEST_TO_NORTH -> renderCorner(length, width, true, mat, emitter);
            case NORTH_TO_WEST, SOUTH_TO_EAST, EAST_TO_NORTH, WEST_TO_SOUTH -> renderCorner(length, width, false, mat, emitter);
            case NORTH_UP, SOUTH_UP, EAST_UP, WEST_UP -> renderSlope(length, width, true, mat, emitter);
            case NORTH_DOWN, SOUTH_DOWN, EAST_DOWN, WEST_DOWN -> renderSlope(length, width, false, mat, emitter);
        }
        return meshBuilder.build();
    }

    public List<BakedQuad> getQuads(final ConveyorOrientation orientation, final Direction face) {
        if (orientation == cachedOrientation) {
            return face == null ? allCachedQuads : cachedQuads.get(face);
        }
        cachedQuads.clear();
        allCachedQuads = new ArrayList<>();
        for (final Direction direction : Direction.values()) {
            cachedQuads.put(direction, new ArrayList<>());
        }
        getMesh(orientation).forEach(quad -> {
            final BakedQuad bakedQuad = quad.toBakedQuad(0, sprite, false);
            if (quad.cullFace() != null) {
                cachedQuads.get(quad.cullFace()).add(bakedQuad);
            }
            allCachedQuads.add(bakedQuad);
        });
        cachedOrientation = orientation;
        return face == null ? allCachedQuads : cachedQuads.get(face);
    }

    private void renderStraight(final float length, final float width, final Matrix3f mat, final QuadEmitter emitter) {
        final int color = 0xFFFFFFFF;
        final float width2 = width / 2.0F;
        emitter.square(Direction.NORTH, 0.5F - width2, 0, 0.5F + width2, length, 15.0F / 16.0F);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);
        emitter.spriteColor(0, color, color, color, color);
        rotate(emitter, mat);
        emitter.emit();

        emitter.square(Direction.SOUTH, 0.5F - width2, 0, 0.5F + width2, length, 0);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED | MutableQuadView.BAKE_ROTATE_180);
        emitter.spriteColor(0, color, color, color, color);
        rotate(emitter, mat);
        emitter.emit();
    }

    private void renderCorner(final float length, final float width, boolean clockwise, final Matrix3f mat, final QuadEmitter emitter) {
        //wtf
        clockwise = !clockwise;
        final int color = 0xFFFFFFFF;
        final float width2 = width / 2.0F;
        emitter.square(Direction.NORTH, 0.5F - width2, 0, 0.5F + width2, length, 15.0F / 16.0F);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, clockwise ? clockWiseSprite : counterClockWiseSprite, MutableQuadView.BAKE_NORMALIZED);
        emitter.spriteColor(0, color, color, color, color);
        rotate(emitter, mat);
        emitter.emit();

        emitter.square(Direction.SOUTH, 0.5F - width2, 0, 0.5F + width2, length, 0);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, clockwise ? clockWiseSprite : counterClockWiseSprite, MutableQuadView.BAKE_NORMALIZED | MutableQuadView.BAKE_ROTATE_180);
        emitter.spriteColor(0, color, color, color, color);
        rotate(emitter, mat);
        emitter.emit();
    }

    private void renderSlope(final float length, final float width, final boolean up, final Matrix3f mat, final QuadEmitter emitter) {
        final int color = 0xFFFFFFFF;
        final float width2 = width / 2.0F;
        emitter.square(Direction.NORTH, 0.5F - width2, 0, 0.5F + width2, length, 15.0F / 16.0F);
        slopify(emitter, up);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);
        emitter.spriteColor(0, color, color, color, color);
        rotate(emitter, mat);
        emitter.emit();

        emitter.square(Direction.SOUTH, 0.5F - width2, 0, 0.5F + width2, length, 0);
        slopify(emitter, up);
        emitter.spriteUnitSquare(0);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED | MutableQuadView.BAKE_ROTATE_180);
        emitter.spriteColor(0, color, color, color, color);
        rotate(emitter, mat);
        emitter.emit();
    }

    private static void rotate(final MutableQuadView quad, final Matrix3f mat) {
        final Vec3f scratch = new Vec3f();
        for (int i = 0; i < 4; i++) {
            quad.copyPos(i, scratch);
            scratch.add(-0.5F, -0.5F, -0.5F);
            scratch.transform(mat);
            scratch.add(0.5F, 0.5F, 0.5F);
            quad.pos(i, scratch);
        }
    }

    private static void slopify(final QuadEmitter emitter, final boolean up) {
        final Vec3f scratch = new Vec3f();
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

    public ConveyorRenderer(final float length, final float width, final Sprite sprite, Sprite clockWiseSprite, final Sprite counterClockWiseSprite) {
        this.length = length;
        this.width = width;
        this.sprite = sprite;
        this.clockWiseSprite = clockWiseSprite;
        this.counterClockWiseSprite = counterClockWiseSprite;
        meshes = new EnumMap<>(ConveyorOrientation.class);
    }
}
