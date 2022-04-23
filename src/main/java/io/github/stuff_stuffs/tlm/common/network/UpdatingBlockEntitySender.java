package io.github.stuff_stuffs.tlm.common.network;

import io.github.stuff_stuffs.tlm.common.TLM;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collection;

public final class UpdatingBlockEntitySender {
    public static final Identifier IDENTIFIER = TLM.createId("block_entity_update");

    public static void send(final BlockEntity blockEntity, final PacketByteBuf buf, final Collection<ServerPlayerEntity> receivers) {
        final PacketByteBuf posBuf = PacketByteBufs.create();
        posBuf.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getRawId(blockEntity.getType()));
        posBuf.writeBlockPos(blockEntity.getPos());
        posBuf.writeBytes(buf);
        final Packet<?> packet = ServerPlayNetworking.createS2CPacket(IDENTIFIER, posBuf);
        for (final ServerPlayerEntity receiver : receivers) {
            ServerPlayNetworking.getSender(receiver).sendPacket(packet);
        }
    }

    private UpdatingBlockEntitySender() {
    }
}
