package io.github.stuff_stuffs.tlm.common.item;

import io.github.stuff_stuffs.tlm.common.api.item.TLMItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class TLMBlockItem extends BlockItem implements TLMItem {
    private final boolean directionalPlacing;
    private final boolean ghostPlacing;

    public TLMBlockItem(final Block block, final Settings settings, final boolean directionalPlacing, final boolean ghostPlacing) {
        super(block, settings);
        this.directionalPlacing = directionalPlacing;
        this.ghostPlacing = ghostPlacing;
    }

    @Override
    public boolean hasDirectionalPlacing() {
        return directionalPlacing;
    }

    @Override
    public boolean hasGhostPlacing() {
        return ghostPlacing;
    }
}
