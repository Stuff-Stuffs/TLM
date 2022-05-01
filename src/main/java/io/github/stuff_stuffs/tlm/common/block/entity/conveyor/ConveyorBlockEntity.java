package io.github.stuff_stuffs.tlm.common.block.entity.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.UpdatingBlockEntity;
import io.github.stuff_stuffs.tlm.common.api.conveyor.*;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.AbstractConveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.MultiSegmentConveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.SlopeCorrectConveyor;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConveyorBlockEntity extends BlockEntity implements ConveyorSupplier, UpdatingBlockEntity {
    public static final float BASE_CONVEYOR_SPEED = 0.125F;
    protected static final byte CONVEYOR_SYNC = 0;
    protected AbstractConveyor conveyor;
    protected boolean initialized = false;

    protected ConveyorBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
        conveyor = createConveyor(pos, state);
    }

    public ConveyorBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.CONVEYOR_BLOCK_ENTITY_TYPE, pos, state);
        conveyor = createConveyor(pos, state);
    }

    @Override
    public void handleUpdate(final PacketByteBuf buf) {
        final byte type = buf.readByte();
        if (type == CONVEYOR_SYNC) {
            conveyor.readSyncFromBuf(buf);
        }
    }

    @Override
    public void update(final Consumer<PacketByteBuf> consumer) {
        if (conveyor.isSyncNeeded()) {
            final PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(CONVEYOR_SYNC);
            conveyor.writeSyncToBuf(buf);
            conveyor.clearSyncFlag();
            consumer.accept(buf);
        }
    }

    protected AbstractConveyor createConveyor(final BlockPos pos, final BlockState state) {
        return createConveyor(pos, state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY), BASE_CONVEYOR_SPEED);
    }

    public static AbstractConveyor createConveyor(final BlockPos pos, final ConveyorOrientation orientation, final float speed) {
        final Vec3d center = Vec3d.ofCenter(orientation.getInputPos(pos));
        final Vec3d outCenter = Vec3d.ofCenter(orientation.getOutputPos(pos));
        final Vec3d in = center.withBias(orientation.getInputSide(), -0.5).add(0, -4 / 12.0, 0);
        final Vec3d out = outCenter.withBias(orientation.getOutputDirection(), -0.5).add(0, -4 / 12.0, 0);
        if (orientation.getType() == ConveyorOrientation.Type.CLOCKWISE_CORNER || orientation.getType() == ConveyorOrientation.Type.COUNTER_CLOCKWISE_CORNER) {
            final Vec3d cornerCenter = Vec3d.ofCenter(orientation.getInputPos(pos).offset(orientation.getInputSide().getOpposite())).add(0, -4 / 12.0, 0);
            return new MultiSegmentConveyor(speed, orientation.getInputSide(), orientation.getOutputDirection(), List.of(in, cornerCenter, out));
        }
        return SlopeCorrectConveyor.create(speed, orientation.getInputSide(), orientation.getOutputDirection(), in, out);
    }

    @Override
    public ConveyorAccess getConveyorAccess() {
        return conveyor;
    }

    @Override
    public Conveyor getConveyor(final Direction side) {
        return conveyor.getConveyor(side);
    }

    @Override
    public void setCachedState(final BlockState state) {
        final boolean diff = state != getCachedState();
        super.setCachedState(state);
        if (diff) {
            conveyor = createConveyor(pos, state);
            initialized = false;
        }
    }

    public static void tick(final World world, final BlockPos pos, final BlockState state, final ConveyorBlockEntity conveyor) {
        if (!conveyor.initialized) {
            final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY);
            final BlockPos inputPos = orientation.getInputPos(pos);
            final BlockPos outputPos = orientation.getOutputPos(pos);
            final Supplier<@Nullable ConveyorLike> inputConveyorLikeCache = ConveyorOrientation.createInputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, orientation.getInputSide().getOpposite(), inputPos, world);
            final Supplier<@Nullable ConveyorLike> outputConveyorLikeCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, orientation.getOutputDirection().getOpposite(), outputPos, world);
            final Supplier<@Nullable Conveyor> outputConveyorCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, orientation.getOutputDirection().getOpposite(), outputPos, world);
            conveyor.conveyor.setup(inputConveyorLikeCache, outputConveyorLikeCache, outputConveyorCache);
            conveyor.initialized = true;
        }
        conveyor.conveyor.tick(TLM.getTickOrder());
        if(conveyor.conveyor.isSyncNeeded()) {
            conveyor.markDirty();
        }
    }
}
