package io.github.stuff_stuffs.tlm.common.block.entity.conveyor;

import io.github.stuff_stuffs.tlm.common.api.UpdatingBlockEntity;
import io.github.stuff_stuffs.tlm.common.api.conveyor.*;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.TwoSplitterConveyor;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.network.UpdatingBlockEntitySender;
import io.github.stuff_stuffs.tlm.common.screen.TwoSplitterBlockScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.AbstractRandom;
import net.minecraft.util.math.random.SimpleRandom;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class TwoSplitterConveyorBlockEntity extends BlockEntity implements UpdatingBlockEntity, ConveyorSupplier, NamedScreenHandlerFactory {
    protected final Map<ConveyorTrayDataStack.State, Choice> dirMap;
    private Choice emptyChoice;
    private int stackIndex = 0;
    private long lastUpdate = -1;
    private AbstractRandom random;
    protected final Map<ConveyorTrayDataStack.State, Boolean> popMap;
    protected TwoSplitterConveyor conveyor;
    protected boolean initialized = false;

    public TwoSplitterConveyorBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.TWO_SPLITTER_CONVEYOR_BLOCK_ENTITY_TYPE, pos, state);
        createConveyor();
        random = new SimpleRandom(0);
        dirMap = new EnumMap<>(ConveyorTrayDataStack.State.class);
        popMap = new EnumMap<>(ConveyorTrayDataStack.State.class);
        for (final ConveyorTrayDataStack.State p : ConveyorTrayDataStack.State.values()) {
            dirMap.put(p, Choice.RANDOM);
            popMap.put(p, false);
        }
        emptyChoice = Choice.RANDOM;
    }

    public void setStack(final int stackIndex) {
        this.stackIndex = stackIndex;
    }

    public int getStack() {
        return stackIndex;
    }

    public void setChoice(@Nullable final ConveyorTrayDataStack.State state, final Choice choice, final boolean pop) {
        if (state == null) {
            emptyChoice = choice;
        } else {
            dirMap.put(state, choice);
            popMap.put(state, pop);
        }
    }

    public Choice getChoice(@Nullable final ConveyorTrayDataStack.State state) {
        if (state == null) {
            return emptyChoice;
        }
        return dirMap.get(state);
    }

    public boolean getPop(@Nullable final ConveyorTrayDataStack.State state) {
        if (state == null) {
            return false;
        }
        return popMap.get(state);
    }

    private void createConveyor() {
        conveyor = new TwoSplitterConveyor(ConveyorBlockEntity.BASE_CONVEYOR_SPEED, getCachedState().get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY).getInputSide(), pos, new TwoSplitterConveyor.Decider() {
            @Override
            public TwoSplitterConveyor.Dir decide(final ConveyorTray tray) {
                final ConveyorTrayDataStack stack = tray.getStack(stackIndex);
                if (stack.isEmpty()) {
                    return compute(emptyChoice);
                }
                return compute(dirMap.get(stack.peek()));
            }

            @Override
            public void onAccept(final ConveyorTray tray) {
                final ConveyorTrayDataStack stack = tray.getStack(stackIndex);
                if (!stack.isEmpty()) {
                    final ConveyorTrayDataStack.State state = stack.peek();
                    if (popMap.get(state)) {
                        stack.pop();
                    }
                }
            }

            private TwoSplitterConveyor.Dir compute(final Choice choice) {
                return switch (choice) {
                    case LEFT -> TwoSplitterConveyor.Dir.LEFT;
                    case RIGHT -> TwoSplitterConveyor.Dir.RIGHT;
                    case RANDOM -> {
                        updateRandom();
                        yield random.nextBoolean() ? TwoSplitterConveyor.Dir.LEFT : TwoSplitterConveyor.Dir.RIGHT;
                    }
                };
            }
        });
    }

    private void updateRandom() {
        final long time = world.getTime();
        if (lastUpdate != time) {
            lastUpdate = time;
            random = new SimpleRandom(time ^ pos.asLong());
        }
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

    @Override
    public Text getDisplayName() {
        return new LiteralText("Two Way Splitter");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(final int syncId, final PlayerInventory inv, final PlayerEntity player) {
        return new TwoSplitterBlockScreenHandler(this, syncId);
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

    public enum Choice {
        LEFT,
        RIGHT,
        RANDOM
    }
}