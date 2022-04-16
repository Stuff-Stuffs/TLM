package io.github.stuff_stuffs.tlm.common.block.entity;

import io.github.stuff_stuffs.tlm.common.api.conveyor.*;
import io.github.stuff_stuffs.tlm.common.block.properties.TLMBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ConveyorBlockEntity extends BlockEntity implements ConveyorSupplier {
    private AbstractConveyor conveyor;
    private boolean initialized = false;

    public ConveyorBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.CONVEYOR_BLOCK_ENTITY_TYPE, pos, state);
        conveyor = createConveyor(pos, state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY));
    }

    private static AbstractConveyor createConveyor(final BlockPos pos, final ConveyorOrientation orientation) {
        final Vec3d center = Vec3d.ofCenter(orientation.getInputPos(pos));
        final Vec3d outCenter = Vec3d.ofCenter(orientation.getOutputPos(pos));
        final Vec3d in = center.withBias(orientation.getInputSide(), -0.5).add(0, -4 / 12.0, 0);
        final Vec3d out = outCenter.withBias(orientation.getOutputDirection(), -0.5).add(0, -4 / 12.0, 0);
        //if (orientation.getType() == ConveyorOrientation.Type.UP_SLOPE) {
        //    final Vec3d outMid = outCenter.withBias(orientation.getOutputDirection(), -0.5).add(0, -4 / 12.0, 0);
        //    return new MultiSegmentConveyor(0.125F, orientation.getInputSide(), orientation.getOutputDirection(), List.of(in, outMid, out));
        //}
        //if (orientation.getType() == ConveyorOrientation.Type.DOWN_SLOPE) {
        //    final Vec3d outMid = in.withBias(orientation.getInputSide(), -0.5).add(0, -4 / 12.0, 0);
        //    return new MultiSegmentConveyor(0.125F, orientation.getInputSide(), orientation.getOutputDirection(), List.of(in, outMid, out));
        //}
        if(orientation.getType()== ConveyorOrientation.Type.CLOCKWISE_CORNER||orientation.getType()== ConveyorOrientation.Type.COUNTER_CLOCKWISE_CORNER) {
            Vec3d cornerCenter = Vec3d.ofCenter(orientation.getInputPos(pos).offset(orientation.getInputSide().getOpposite())).add(0, -4 / 12.0, 0);
            return new MultiSegmentConveyor(0.125F, orientation.getInputSide(), orientation.getOutputDirection(), List.of(in, cornerCenter, out));
        }
        return new SimpleConveyor(0.125F, orientation.getInputSide(), orientation.getOutputDirection(), in, out, 1);
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
            conveyor = createConveyor(pos, state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY));
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
        conveyor.conveyor.tick();
        if (conveyor.pos.equals(BlockPos.ORIGIN)) {
            final ConveyorTray tray = new ConveyorTray();
            conveyor.conveyor.getConveyor(null).tryInsert(tray, 1);
        }
    }
}
