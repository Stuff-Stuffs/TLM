package io.github.stuff_stuffs.tlm.client.screen;

import io.github.stuff_stuffs.tlm.client.screen.widget.IconWidget;
import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import io.github.stuff_stuffs.tlm.common.screen.TwoSplitterBlockScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class TwoSplitterBlockHandledScreen extends HandledScreen<TwoSplitterBlockScreenHandler> {
    private final PlayerEntity entity;
    private ButtonWidget redChoiceButton, greenChoiceButton, blueChoiceButton, yellowChoiceButton;

    public TwoSplitterBlockHandledScreen(final TwoSplitterBlockScreenHandler handler, final PlayerInventory inventory, final Text title) {
        super(handler, inventory, title);
        entity = inventory.player;
    }

    @Override
    protected void init() {
        super.init();
        final int centerX = width / 2;
        final int centerY = height / 2;
        final Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(TLM.createId("conveyor/stack_marker"));
        final Supplier<Sprite> spriteSupplier = () -> sprite;
        final IconWidget redIcon = new IconWidget(centerX, centerY, 16, 16, spriteSupplier, () -> ConveyorTrayDataStack.State.RED.color);
        final IconWidget greenIcon = new IconWidget(centerX, centerY + 18, 16, 16, spriteSupplier, () -> ConveyorTrayDataStack.State.GREEN.color);
        final IconWidget blueIcon = new IconWidget(centerX, centerY + 18 * 2, 16, 16, spriteSupplier, () -> ConveyorTrayDataStack.State.BLUE.color);
        final IconWidget yellowIcon = new IconWidget(centerX, centerY + 18 * 3, 16, 16, spriteSupplier, () -> ConveyorTrayDataStack.State.YELLOW.color);
        redChoiceButton = new ButtonWidget(centerX + 18, centerY, 16, 16, new LiteralText(handler.getChoice(ConveyorTrayDataStack.State.RED).name()), button -> handler.onButtonClick(entity, TwoSplitterBlockScreenHandler.CHOICE_RED_BUTTON_ID));
        greenChoiceButton = new ButtonWidget(centerX + 18, centerY + 18, 16, 16, new LiteralText(handler.getChoice(ConveyorTrayDataStack.State.GREEN).name()), button -> handler.onButtonClick(entity, TwoSplitterBlockScreenHandler.CHOICE_GREEN_BUTTON_ID));
        blueChoiceButton = new ButtonWidget(centerX + 18, centerY + 18 * 2, 16, 16, new LiteralText(handler.getChoice(ConveyorTrayDataStack.State.BLUE).name()), button -> handler.onButtonClick(entity, TwoSplitterBlockScreenHandler.CHOICE_BLUE_BUTTON_ID));
        yellowChoiceButton = new ButtonWidget(centerX + 18, centerY + 18 * 3, 16, 16, new LiteralText(handler.getChoice(ConveyorTrayDataStack.State.YELLOW).name()), button -> handler.onButtonClick(entity, TwoSplitterBlockScreenHandler.CHOICE_YELLOW_BUTTON_ID));
        addDrawable(redIcon);
        addDrawable(greenIcon);
        addDrawable(blueIcon);
        addDrawable(yellowIcon);
        addDrawableChild(redChoiceButton);
        addDrawableChild(greenChoiceButton);
        addDrawableChild(blueChoiceButton);
        addDrawableChild(yellowChoiceButton);
    }

    @Override
    protected void drawBackground(final MatrixStack matrices, final float delta, final int mouseX, final int mouseY) {
        redChoiceButton.setMessage(new LiteralText(handler.getChoice(ConveyorTrayDataStack.State.RED).name()));
        greenChoiceButton.setMessage(new LiteralText(handler.getChoice(ConveyorTrayDataStack.State.GREEN).name()));
        blueChoiceButton.setMessage(new LiteralText(handler.getChoice(ConveyorTrayDataStack.State.BLUE).name()));
        yellowChoiceButton.setMessage(new LiteralText(handler.getChoice(ConveyorTrayDataStack.State.YELLOW).name()));
    }
}
