package io.github.stuff_stuffs.tlm.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class IconWidget extends DrawableHelper implements Drawable, Element {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Supplier<Sprite> spriteSupplier;
    private final IntSupplier tintSupplier;

    public IconWidget(final int x, final int y, final int width, final int height, final Supplier<Sprite> spriteSupplier, final IntSupplier tintSupplier) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.spriteSupplier = spriteSupplier;
        this.tintSupplier = tintSupplier;
    }

    @Override
    public void render(final MatrixStack matrices, final int mouseX, final int mouseY, final float delta) {
        final Sprite sprite = spriteSupplier.get();
        final int tint = tintSupplier.getAsInt();
        final Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableTexture();
        final BufferBuilder buffer = tessellator.getBuffer();
        final Matrix4f posMat = matrices.peek().getPositionMatrix();
        final int x0 = x;
        final int y0 = y;
        final int x1 = x + width;
        final int y1 = y + height;
        final float u0 = sprite.getMinU();
        final float v0 = sprite.getMinV();
        final float u1 = sprite.getMaxU();
        final float v1 = sprite.getMaxV();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);

        buffer.vertex(posMat, x0, y1, 1).color(tint).texture(u0, v1).next();
        buffer.vertex(posMat, x1, y1, 1).color(tint).texture(u1, v1).next();
        buffer.vertex(posMat, x1, y0, 1).color(tint).texture(u1, v0).next();
        buffer.vertex(posMat, x0, y0, 1).color(tint).texture(u0, v0).next();

        tessellator.draw();
    }
}
