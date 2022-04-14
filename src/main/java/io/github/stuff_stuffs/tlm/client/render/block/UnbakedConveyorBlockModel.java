package io.github.stuff_stuffs.tlm.client.render.block;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class UnbakedConveyorBlockModel implements UnbakedModel {
    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(final Function<Identifier, UnbakedModel> unbakedModelGetter, final Set<Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public BakedModel bake(final ModelLoader loader, final Function<SpriteIdentifier, Sprite> textureGetter, final ModelBakeSettings rotationContainer, final Identifier modelId) {
        return new BakedConveyorBlockModel();
    }
}
