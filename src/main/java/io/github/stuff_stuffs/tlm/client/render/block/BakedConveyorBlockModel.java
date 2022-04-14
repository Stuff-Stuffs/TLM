package io.github.stuff_stuffs.tlm.client.render.block;

import io.github.stuff_stuffs.tlm.client.render.conveyor.ConveyorRenderer;
import io.github.stuff_stuffs.tlm.common.block.properties.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.properties.TLMBlockProperties;
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

public class BakedConveyorBlockModel implements BakedModel, FabricBakedModel {
    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(final BlockRenderView blockView, final BlockState state, final BlockPos pos, final Supplier<AbstractRandom> randomSupplier, final RenderContext context) {
        final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY);
        ConveyorRenderer.render(1, 1, orientation, context);
    }

    @Override
    public void emitItemQuads(final ItemStack stack, final Supplier<AbstractRandom> randomSupplier, final RenderContext context) {
        ConveyorRenderer.render(1, 1, ConveyorOrientation.NORTH, context);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction face, final AbstractRandom random) {
        throw new UnsupportedOperationException();
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
        return ConveyorRenderer.SPRITE_SUPPLIER.get();
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
