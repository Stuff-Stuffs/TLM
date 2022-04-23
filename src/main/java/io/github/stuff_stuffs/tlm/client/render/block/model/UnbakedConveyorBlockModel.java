package io.github.stuff_stuffs.tlm.client.render.block.model;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tlm.common.TLM;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class UnbakedConveyorBlockModel implements UnbakedModel {
    public static final SpriteIdentifier SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, TLM.createId("block/conveyor_belt"));
    public static final SpriteIdentifier CLOCKWISE_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, TLM.createId("block/conveyor_belt_cw"));
    public static final SpriteIdentifier COUNTER_CLOCKWISE_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, TLM.createId("block/conveyor_belt_ccw"));

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(final Function<Identifier, UnbakedModel> unbakedModelGetter, final Set<Pair<String, String>> unresolvedTextureReferences) {
        return List.of(SPRITE, CLOCKWISE_SPRITE, COUNTER_CLOCKWISE_SPRITE);
    }

    @Nullable
    @Override
    public BakedModel bake(final ModelLoader loader, final Function<SpriteIdentifier, Sprite> textureGetter, final ModelBakeSettings rotationContainer, final Identifier modelId) {
        return new BakedConveyorBlockModel(textureGetter.apply(SPRITE), textureGetter.apply(CLOCKWISE_SPRITE), textureGetter.apply(COUNTER_CLOCKWISE_SPRITE));
    }
}
