package io.github.stuff_stuffs.tlm.common.network;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.mixin.impl.MixinThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.*;

public final class UpdatingBlockEntitySender {
    public static final Identifier IDENTIFIER = TLM.createId("block_entity_update");
    private static final Map<ServerWorld, List<Data>> BUFFERS_BY_WORLD = new WeakHashMap<>();

    public static void send(final BlockEntity blockEntity, final PacketByteBuf buf, final Collection<ServerPlayerEntity> receivers) {
        final PacketByteBuf posBuf = PacketByteBufs.create();
        posBuf.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getRawId(blockEntity.getType()));
        posBuf.writeBlockPos(blockEntity.getPos());
        posBuf.writeVarInt(buf.writerIndex());
        posBuf.writeBytes(buf);

        final World world = blockEntity.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            throw new RuntimeException("Cannot update block entity on client");
        }
        BUFFERS_BY_WORLD.computeIfAbsent(serverWorld, w -> new ArrayList<>()).add(new Data(blockEntity.getPos(), posBuf));
    }

    public static void tick() {
        for (final Map.Entry<ServerWorld, List<Data>> entry : BUFFERS_BY_WORLD.entrySet()) {
            tickWorld(entry.getKey(), entry.getValue());
        }
        BUFFERS_BY_WORLD.clear();
    }

    private static void tickWorld(final ServerWorld world, final List<Data> data) {
        final Collection<ServerPlayerEntity> entities = PlayerLookup.world(world);
        final Map<ServerPlayerEntity, PacketByteBuf> buffers = new Reference2ReferenceOpenHashMap<>(entities.size());
        final int watchDistance = ((MixinThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage).getWatchDistance();
        for (final Data datum : data) {
            final ChunkPos chunkPos = new ChunkPos(datum.pos());
            for (final ServerPlayerEntity entity : entities) {
                final ChunkSectionPos watchedPos = entity.getWatchedSection();
                if (ThreadedAnvilChunkStorage.isWithinDistance(watchedPos.getX(), watchedPos.getZ(), chunkPos.x, chunkPos.z, watchDistance)) {
                    final PacketByteBuf buf = buffers.computeIfAbsent(entity, i -> PacketByteBufs.create());
                    buf.writeBytes(datum.buf());
                }
            }
        }
        for (final Map.Entry<ServerPlayerEntity, PacketByteBuf> entry : buffers.entrySet()) {
            ServerPlayNetworking.send(entry.getKey(), IDENTIFIER, entry.getValue());
        }
    }

    private record Data(BlockPos pos, PacketByteBuf buf) {
    }

    private UpdatingBlockEntitySender() {
    }
}
