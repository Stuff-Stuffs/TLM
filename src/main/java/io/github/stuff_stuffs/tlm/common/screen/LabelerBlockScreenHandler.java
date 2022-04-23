package io.github.stuff_stuffs.tlm.common.screen;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import io.github.stuff_stuffs.tlm.common.block.entity.conveyor.LabelerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;

public class LabelerBlockScreenHandler extends ScreenHandler {
    public static final int SWITCH_LABEL_STATE_BUTTON_ID = 0;
    private final LabelerBlockEntity labeler;
    private final Property labelState;
    private int clientLabelState;

    public LabelerBlockScreenHandler(final int syncId) {
        super(TLMScreenHandlerTypes.LABELER_BLOCK_CONFIGURATION_SCREEN_HANDLER_TYPE, syncId);
        labeler = null;
        labelState = new Property() {
            @Override
            public int get() {
                return clientLabelState;
            }

            @Override
            public void set(final int value) {
                clientLabelState = value;
            }
        };
        addProperty(labelState);
    }

    public LabelerBlockScreenHandler(final LabelerBlockEntity labeler, final int syncId) {
        super(TLMScreenHandlerTypes.LABELER_BLOCK_CONFIGURATION_SCREEN_HANDLER_TYPE, syncId);
        this.labeler = labeler;
        labelState = new Property() {
            @Override
            public int get() {
                return labeler.getLabelState().idx;
            }

            @Override
            public void set(final int value) {
                labeler.setLabelState(ConveyorTrayDataStack.State.getByIdx(value));
            }
        };
        addProperty(labelState);
    }

    @Override
    public boolean onButtonClick(final PlayerEntity player, final int id) {
        if (isClient()) {
            MinecraftClient.getInstance().interactionManager.clickButton(syncId, id);
        } else if (isServer()) {
            if (id == SWITCH_LABEL_STATE_BUTTON_ID) {
                labelState.set((labelState.get() + 1) % ConveyorTrayDataStack.State.values().length);
                sendContentUpdates();
            }
        }
        return true;
    }

    public ConveyorTrayDataStack.State getLabelState() {
        return ConveyorTrayDataStack.State.getByIdx(labelState.get());
    }

    public boolean isClient() {
        return labeler == null;
    }

    public boolean isServer() {
        return labeler != null;
    }

    @Override
    public boolean canUse(final PlayerEntity player) {
        return true;
    }
}
