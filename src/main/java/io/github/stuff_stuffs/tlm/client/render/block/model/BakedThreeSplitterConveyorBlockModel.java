package io.github.stuff_stuffs.tlm.client.render.block.model;

import io.github.stuff_stuffs.tlm.client.render.conveyor.ConveyorRenderer;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.AbstractRandom;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class BakedThreeSplitterConveyorBlockModel implements BakedModel, FabricBakedModel {
    private final Sprite sprite;
    private final ConveyorRenderer renderer;

    public BakedThreeSplitterConveyorBlockModel(final Sprite sprite, final Sprite clockWiseSprite, final Sprite counterClockWiseSprite) {
        this.sprite = sprite;
        renderer = new ConveyorRenderer(1, 1, sprite, clockWiseSprite, counterClockWiseSprite);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(final BlockRenderView blockView, final BlockState state, final BlockPos pos, final Supplier<AbstractRandom> randomSupplier, final RenderContext context) {
        final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
        context.meshConsumer().accept(renderer.getMesh(orientation));
    }

    @Override
    public void emitItemQuads(final ItemStack stack, final Supplier<AbstractRandom> randomSupplier, final RenderContext context) {
        context.meshConsumer().accept(renderer.getMesh(ConveyorOrientation.NORTH));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction face, final AbstractRandom random) {
        if (state == null) {
            return List.of();
        }
        final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
        return renderer.getQuads(orientation, face);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return sprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}
