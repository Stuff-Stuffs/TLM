package io.github.stuff_stuffs.tlm.common.screen;

import io.github.stuff_stuffs.tlm.common.TLM;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;

public final class TLMScreenHandlerTypes {
    public static final ScreenHandlerType<LabelerBlockScreenHandler> LABELER_BLOCK_CONFIGURATION_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>((syncId, playerInventory) -> new LabelerBlockScreenHandler(syncId));

    public static void init() {
        Registry.register(Registry.SCREEN_HANDLER, TLM.createId("labeler_config"), LABELER_BLOCK_CONFIGURATION_SCREEN_HANDLER_TYPE);
    }

    private TLMScreenHandlerTypes() {
    }
}
