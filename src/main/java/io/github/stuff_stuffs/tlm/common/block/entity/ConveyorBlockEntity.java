package io.github.stuff_stuffs.tlm.common.block.entity;

import io.github.stuff_stuffs.tlm.common.api.UnsidedBlockApiCache;
import io.github.stuff_stuffs.tlm.common.api.conveyor.*;
import io.github.stuff_stuffs.tlm.common.block.properties.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.properties.TLMBlockProperties;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ConveyorBlockEntity extends BlockEntity implements ConveyorSupplier {
    private SimpleConveyor conveyor;
    private boolean initialized = false;

    public ConveyorBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.CONVEYOR_BLOCK_ENTITY_TYPE, pos, state);
        conveyor = createConveyor(pos, state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY));
    }

    private static SimpleConveyor createConveyor(final BlockPos pos, final ConveyorOrientation orientation) {
        final Vec3d center = Vec3d.ofCenter(pos);
        final Vec3d outCenter = Vec3d.ofCenter(orientation.getOutputPos(pos));
        final Vec3d in = center.withBias(orientation.getInputSide(), 0.5);
        final Vec3d out = outCenter.withBias(orientation.getOutputDirection(), -0.5);
        return new SimpleConveyor(0.125F, orientation.getInputSide(), orientation.getOutputDirection(), in.add(0, 1, 0), out.add(0, 1, 0));
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
            final BlockPos inputPos = pos.offset(orientation.getInputSide().getOpposite());
            final BlockPos outputPos = orientation.getOutputPos(pos);
            final BlockApiCache<ConveyorLike, Direction> inputConveyorLikeCache = UnsidedBlockApiCache.getUnsidedCache(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, world, inputPos);
            final BlockApiCache<ConveyorLike, Direction> outputConveyorLikeCache = UnsidedBlockApiCache.getUnsidedCache(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, world, outputPos);
            final BlockApiCache<Conveyor, Direction> outputConveyorCache = UnsidedBlockApiCache.getUnsidedCache(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, world, outputPos);
            conveyor.conveyor.setup(
                    () -> inputConveyorLikeCache.find(orientation.getInputSide().getOpposite()),
                    () -> outputConveyorLikeCache.find(orientation.getOutputDirection().getOpposite()),
                    () -> outputConveyorCache.find(orientation.getOutputDirection().getOpposite())
            );
            conveyor.initialized = true;
        }
        conveyor.conveyor.tick();
        if (conveyor.pos.equals(BlockPos.ORIGIN)) {
            final ConveyorTray tray = new ConveyorTray();
            conveyor.conveyor.getConveyor(null).tryInsert(tray, 1);
        }
    }
}
