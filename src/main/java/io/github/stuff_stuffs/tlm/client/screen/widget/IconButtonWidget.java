package io.github.stuff_stuffs.tlm.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class IconButtonWidget extends ButtonWidget {
    private final Supplier<Sprite> spriteGetter;
    private final IntSupplier tintGetter;

    public IconButtonWidget(final int x, final int y, final int width, final int height, final PressAction onPress, final TooltipSupplier tooltipSupplier, final Supplier<Sprite> spriteGetter, final IntSupplier tintGetter) {
        super(x, y, width, height, Text.of(""), onPress, tooltipSupplier);
        this.spriteGetter = spriteGetter;
        this.tintGetter = tintGetter;
    }

    @Override
    public void renderButton(final MatrixStack matrices, final int mouseX, final int mouseY, final float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        final int colour = tintGetter.getAsInt();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        final Sprite sprite = spriteGetter.get();
        drawTexturedQuad(matrices.peek().getPositionMatrix(), x, x + width, y, y + height, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), colour);
    }

    private static void drawTexturedQuad(final Matrix4f matrix, final int x0, final int x1, final int y0, final int y1, final float u0, final float u1, final float v0, final float v1, final int colour) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableTexture();
        final BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix, x0, y1, 1).color(colour).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x1, y1, 1).color(colour).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x1, y0, 1).color(colour).texture(u1, v0).next();
        bufferBuilder.vertex(matrix, x0, y0, 1).color(colour).texture(u0, v0).next();
        Tessellator.getInstance().draw();
    }
}
