package io.github.stuff_stuffs.tlm.common;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import io.github.stuff_stuffs.tlm.common.block.TLMBlocks;
import io.github.stuff_stuffs.tlm.common.item.TLMItems;
import io.github.stuff_stuffs.tlm.common.screen.TLMScreenHandlerTypes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TLM implements ModInitializer {
    public static final String MOD_ID = "tlm";
    public static final Logger LOGGER = LoggerFactory.getLogger("TLM");

    @Override
    public void onInitialize() {
        ConveyedResourceType.init();
        TLMBlocks.init();
        TLMItems.init();
        TLMScreenHandlerTypes.init();
    }

    public static Identifier createId(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
