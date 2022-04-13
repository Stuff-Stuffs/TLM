package io.github.stuff_stuffs.tlm.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class TLMBlockItem extends BlockItem implements TLMItem {
    private final boolean directionalPlacing;
    public TLMBlockItem(Block block, Settings settings, boolean directionalPlacing) {
        super(block, settings);
        this.directionalPlacing = directionalPlacing;
    }

    @Override
    public boolean hasDirectionalPlacing() {
        return directionalPlacing;
    }
}
