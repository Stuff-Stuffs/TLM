package io.github.stuff_stuffs.tlm.common.block.entity.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorApi;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorLike;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.AbstractConveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.LabelerConveyor;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.screen.LabelerBlockScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LabelerBlockEntity extends ConveyorBlockEntity implements NamedScreenHandlerFactory {
    private ConveyorTrayDataStack.State labelState;

    public LabelerBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.LABELER_BLOCK_ENTITY_BLOCK_TYPE, pos, state);
        labelState = ConveyorTrayDataStack.State.RED;
    }

    @Override
    protected void writeNbt(final NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("state", labelState.idx);
    }

    @Override
    public void readNbt(final NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("state", NbtElement.INT_TYPE)) {
            labelState = ConveyorTrayDataStack.State.getByIdx(nbt.getInt("state"));
            ((LabelerConveyor) conveyor).setLabel(labelState);
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        final NbtCompound compound = super.toInitialChunkDataNbt();
        compound.putInt("state", labelState.idx);
        return compound;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public ConveyorTrayDataStack.State getLabelState() {
        return labelState;
    }

    public void setLabelState(final ConveyorTrayDataStack.State labelState) {
        this.labelState = labelState;
        markDirty();
        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        ((LabelerConveyor) conveyor).setLabel(labelState);
    }

    @Override
    protected AbstractConveyor createConveyor(final BlockPos pos, final BlockState state) {
        final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
        final Vec3d center = Vec3d.ofCenter(orientation.getInputPos(pos));
        final Vec3d outCenter = Vec3d.ofCenter(orientation.getOutputPos(pos));
        final Vec3d in = center.withBias(orientation.getInputSide(), -0.5).add(0, -4 / 12.0, 0);
        final Vec3d out = outCenter.withBias(orientation.getOutputDirection(), -0.5).add(0, -4 / 12.0, 0);
        return new LabelerConveyor(BASE_CONVEYOR_SPEED, orientation.getInputSide(), orientation.getOutputDirection(), in, out, labelState == null ? ConveyorTrayDataStack.State.RED : labelState, 0);
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Labeler");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(final int syncId, final PlayerInventory inv, final PlayerEntity player) {
        return new LabelerBlockScreenHandler(this, syncId);
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
        conveyor.conveyor.tick(TLM.getTickOrder());
        if (conveyor.conveyor.isSyncNeeded()) {
            conveyor.markDirty();
        }
    }
}
