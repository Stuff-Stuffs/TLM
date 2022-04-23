package io.github.stuff_stuffs.tlm.client.screen;

import io.github.stuff_stuffs.tlm.client.screen.widget.IconButtonWidget;
import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.screen.LabelerBlockScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class LabelerBlockHandledScreen extends HandledScreen<LabelerBlockScreenHandler> {
    private final PlayerEntity entity;

    public LabelerBlockHandledScreen(final LabelerBlockScreenHandler handler, final PlayerInventory inventory, final Text title) {
        super(handler, inventory, title);
        entity = inventory.player;
    }

    @Override
    protected void init() {
        super.init();
        addDrawableChild(new IconButtonWidget(width / 2 - 8, height / 2 - 8, 16, 16, button -> handler.onButtonClick(entity, LabelerBlockScreenHandler.SWITCH_LABEL_STATE_BUTTON_ID), (button, matrices, mouseX, mouseY) -> {
        }, () -> MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(TLM.createId("conveyor/stack_marker")), () -> handler.getLabelState().color));
    }

    @Override
    protected void drawBackground(final MatrixStack matrices, final float delta, final int mouseX, final int mouseY) {

    }
}
