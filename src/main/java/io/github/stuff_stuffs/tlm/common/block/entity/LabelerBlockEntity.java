package io.github.stuff_stuffs.tlm.common.block.entity;

import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorApi;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorLike;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.AbstractConveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.LabelerConveyor;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.network.UpdatingBlockEntitySender;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

public class LabelerBlockEntity extends ConveyorBlockEntity {
    public LabelerBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.LABELER_BLOCK_ENTITY_BLOCK_TYPE, pos, state);
    }

    @Override
    protected AbstractConveyor createConveyor(final BlockPos pos, final BlockState state) {
        final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
        final Vec3d center = Vec3d.ofCenter(orientation.getInputPos(pos));
        final Vec3d outCenter = Vec3d.ofCenter(orientation.getOutputPos(pos));
        final Vec3d in = center.withBias(orientation.getInputSide(), -0.5).add(0, -4 / 12.0, 0);
        final Vec3d out = outCenter.withBias(orientation.getOutputDirection(), -0.5).add(0, -4 / 12.0, 0);
        return new LabelerConveyor(BASE_CONVEYOR_SPEED, orientation.getInputSide(), orientation.getOutputDirection(), in, out, ConveyorTrayDataStack.State.RED, 0);
    }

    public static void tick(final World world, final BlockPos pos, final BlockState state, final ConveyorBlockEntity conveyor) {
        if (!conveyor.initialized) {
            final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
            final BlockPos inputPos = orientation.getInputPos(pos);
            final BlockPos outputPos = orientation.getOutputPos(pos);
            final Supplier<@Nullable ConveyorLike> inputConveyorLikeCache = ConveyorOrientation.createInputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, orientation.getInputSide().getOpposite(), inputPos, world);
            final Supplier<@Nullable ConveyorLike> outputConveyorLikeCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, orientation.getOutputDirection().getOpposite(), outputPos, world);
            final Supplier<@Nullable Conveyor> outputConveyorCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, orientation.getOutputDirection().getOpposite(), outputPos, world);
            conveyor.conveyor.setup(inputConveyorLikeCache, outputConveyorLikeCache, outputConveyorCache);
            conveyor.initialized = true;
        }
        conveyor.conveyor.tick();
        if (!world.isClient() && conveyor.conveyor.isSyncNeeded()) {
            final Collection<ServerPlayerEntity> tracking = PlayerLookup.tracking(conveyor);
            if (tracking.isEmpty()) {
                return;
            }
            final PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(CONVEYOR_SYNC);
            conveyor.conveyor.writeSyncToBuf(buf);
            UpdatingBlockEntitySender.send(conveyor, buf, tracking);
            conveyor.conveyor.clearSyncFlag();
        }
    }
}
