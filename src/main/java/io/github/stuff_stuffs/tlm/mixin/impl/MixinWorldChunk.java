package io.github.stuff_stuffs.tlm.mixin.impl;

import io.github.stuff_stuffs.tlm.common.api.event.BlockEntityEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldChunk.class)
public class MixinWorldChunk {
    @Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;markRemoved()V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onBlockEntityRemove(final BlockPos pos, final CallbackInfo ci, @Nullable final BlockEntity removed) {
        if (removed != null) {
            BlockEntityEvents.REMOVE.invoker().onRemove(removed);
        }
    }
}
