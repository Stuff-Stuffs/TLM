package io.github.stuff_stuffs.tlm.common.block.entity.conveyor;

import io.github.stuff_stuffs.tlm.common.api.UpdatingBlockEntity;
import io.github.stuff_stuffs.tlm.common.api.conveyor.*;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.TwoSplitterConveyor;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.network.UpdatingBlockEntitySender;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.AbstractRandom;
import net.minecraft.util.math.random.SimpleRandom;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

public class TwoSplitterConveyorBlockEntity extends BlockEntity implements UpdatingBlockEntity, ConveyorSupplier {
    protected TwoSplitterConveyor conveyor;
    protected boolean initialized = false;

    public TwoSplitterConveyorBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.TWO_SPLITTER_CONVEYOR_BLOCK_ENTITY_TYPE, pos, state);
        createConveyor();
    }

    private void createConveyor() {
        final AbstractRandom random = new SimpleRandom(pos.asLong());
        conveyor = new TwoSplitterConveyor(ConveyorBlockEntity.BASE_CONVEYOR_SPEED, getCachedState().get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY).getInputSide(), pos, new TwoSplitterConveyor.Decider() {
            @Override
            public TwoSplitterConveyor.Dir decide(final ConveyorTray tray) {
                return random.nextBoolean() ? TwoSplitterConveyor.Dir.LEFT : TwoSplitterConveyor.Dir.RIGHT;
            }

            @Override
            public void onAccept(final ConveyorTray tray) {

            }
        });
    }

    @Override
    public void setCachedState(final BlockState state) {
        final boolean diff = state != getCachedState();
        super.setCachedState(state);
        if (diff) {
            createConveyor();
            initialized = false;
        }
    }

    @Override
    public void handleUpdate(final PacketByteBuf buf) {
        final byte type = buf.readByte();
        if (type == ConveyorBlockEntity.CONVEYOR_SYNC) {
            conveyor.readSyncFromBuf(buf);
        }
    }

    @Override
    public ConveyorAccess getConveyorAccess() {
        return conveyor;
    }

    @Override
    public Conveyor getConveyor(final Direction side) {
        return conveyor.getConveyor(side);
    }

    public static void tick(final World world, final BlockPos pos, final BlockState state, final TwoSplitterConveyorBlockEntity conveyor) {
        if (!conveyor.initialized) {
            final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
            final BlockPos inputPos = orientation.getInputPos(pos);
            final Supplier<@Nullable ConveyorLike> inputConveyorLikeCache = ConveyorOrientation.createInputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, orientation.getInputSide().getOpposite(), inputPos, world);
            final Direction leftDirection = orientation.getOutputDirection().getOpposite().rotateYClockwise();
            final Direction rightDirection = orientation.getOutputDirection().getOpposite().rotateYCounterclockwise();
            final Supplier<@Nullable ConveyorLike> output0ConveyorLikeCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, leftDirection, pos.offset(rightDirection), world);
            final Supplier<@Nullable Conveyor> output0ConveyorCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, leftDirection, pos.offset(rightDirection), world);
            final Supplier<@Nullable ConveyorLike> output1ConveyorLikeCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, rightDirection, pos.offset(leftDirection), world);
            final Supplier<@Nullable Conveyor> output1ConveyorCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, rightDirection, pos.offset(leftDirection), world);
            conveyor.conveyor.setup(inputConveyorLikeCache, output0ConveyorLikeCache, output0ConveyorCache, output1ConveyorLikeCache, output1ConveyorCache);
            conveyor.initialized = true;
        }
        conveyor.conveyor.tick();
        if (!world.isClient() && conveyor.conveyor.isSyncNeeded()) {
            final Collection<ServerPlayerEntity> tracking = PlayerLookup.tracking(conveyor);
            if (tracking.isEmpty()) {
                return;
            }
            final PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(ConveyorBlockEntity.CONVEYOR_SYNC);
            conveyor.conveyor.writeSyncToBuf(buf);
            UpdatingBlockEntitySender.send(conveyor, buf, tracking);
            conveyor.conveyor.clearSyncFlag();
        }
    }
}
