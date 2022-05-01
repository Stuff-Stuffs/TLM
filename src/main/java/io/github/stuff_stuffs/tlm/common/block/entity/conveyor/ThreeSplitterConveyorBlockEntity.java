package io.github.stuff_stuffs.tlm.common.block.entity.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.UpdatingBlockEntity;
import io.github.stuff_stuffs.tlm.common.api.conveyor.*;
import io.github.stuff_stuffs.tlm.common.api.conveyor.impls.ThreeSplitterConveyor;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.screen.ThreeSplitterBlockScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.AbstractRandom;
import net.minecraft.util.math.random.SimpleRandom;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ThreeSplitterConveyorBlockEntity extends BlockEntity implements UpdatingBlockEntity, ConveyorSupplier, NamedScreenHandlerFactory {
    private final Map<ConveyorTrayDataStack.State, Choice> dirMap;
    private Choice emptyChoice;
    private int stackIndex = 0;
    private long lastUpdate = -1;
    private final AbstractRandom random;
    private final Map<ConveyorTrayDataStack.State, Boolean> popMap;
    private ThreeSplitterConveyor conveyor;
    private boolean initialized = false;

    public ThreeSplitterConveyorBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.THREE_SPLITTER_CONVEYOR_BLOCK_ENTITY_TYPE, pos, state);
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
        conveyor = new ThreeSplitterConveyor(ConveyorBlockEntity.BASE_CONVEYOR_SPEED, getCachedState().get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY).getInputSide(), pos, new ThreeSplitterConveyor.Decider() {
            @Override
            public ThreeSplitterConveyor.Dir decide(final ConveyorTray tray) {
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

            private ThreeSplitterConveyor.Dir compute(final Choice choice) {
                return switch (choice) {
                    case LEFT -> ThreeSplitterConveyor.Dir.LEFT;
                    case RIGHT -> ThreeSplitterConveyor.Dir.RIGHT;
                    case STRAIGHT -> ThreeSplitterConveyor.Dir.STRAIGHT;
                    case RANDOM -> {
                        updateRandom();
                        final int i = random.nextInt(3);
                        yield switch (i) {
                            case 0 -> ThreeSplitterConveyor.Dir.LEFT;
                            case 1 -> ThreeSplitterConveyor.Dir.STRAIGHT;
                            default -> ThreeSplitterConveyor.Dir.RIGHT;
                        };
                    }
                };
            }
        });
    }

    private void updateRandom() {
        final long time = world.getTime();
        if (lastUpdate != time) {
            lastUpdate = time;
            random.setSeed(world.getTime() ^ pos.asLong());
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
    protected void writeNbt(final NbtCompound nbt) {
        final NbtCompound dirMap = new NbtCompound();
        for (final ConveyorTrayDataStack.State state : ConveyorTrayDataStack.State.values()) {
            final NbtCompound compound = new NbtCompound();
            compound.putString("choice", this.dirMap.get(state).name());
            compound.putBoolean("pop", popMap.get(state));
            dirMap.put(state.name(), compound);
        }
        final NbtCompound empty = new NbtCompound();
        empty.putString("choice", emptyChoice.name());
        dirMap.put("empty", empty);
        nbt.put("choices", dirMap);
        final NbtCompound conveyor = new NbtCompound();
        this.conveyor.writeToNbt(conveyor);
        nbt.put("conveyor", conveyor);
    }

    @Override
    public void readNbt(final NbtCompound nbt) {
        if (nbt.contains("choices")) {
            final NbtCompound sub = nbt.getCompound("choices");
            for (final ConveyorTrayDataStack.State state : ConveyorTrayDataStack.State.values()) {
                final NbtCompound compound = sub.getCompound(state.name());
                dirMap.put(state, Choice.valueOf(compound.getString("choice")));
                if (compound.contains("pop", NbtElement.BYTE_TYPE)) {
                    popMap.put(state, compound.getBoolean("pop"));
                } else {
                    popMap.put(state, false);
                }
            }
            emptyChoice = Choice.valueOf(sub.getCompound("empty").getString("choice"));
        }
        if (nbt.contains("conveyor")) {
            conveyor.readFromNbt(nbt.getCompound("conveyor"));
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
    public void update(final Consumer<PacketByteBuf> consumer) {
        if (conveyor.isSyncNeeded()) {
            final PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(ConveyorBlockEntity.CONVEYOR_SYNC);
            conveyor.writeSyncToBuf(buf);
            conveyor.clearSyncFlag();
            consumer.accept(buf);
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
        return Text.of("Three Way Splitter");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(final int syncId, final PlayerInventory inv, final PlayerEntity player) {
        return new ThreeSplitterBlockScreenHandler(this, syncId);
    }

    public static void tick(final World world, final BlockPos pos, final BlockState state, final ThreeSplitterConveyorBlockEntity conveyor) {
        if (!conveyor.initialized) {
            final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
            final BlockPos inputPos = orientation.getInputPos(pos);
            final Supplier<@Nullable ConveyorLike> inputConveyorLikeCache = ConveyorOrientation.createInputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, orientation.getInputSide().getOpposite(), inputPos, world);
            final Direction outputDirection = orientation.getOutputDirection();
            final Direction outputSide = outputDirection.getOpposite();
            final Direction leftDirection = outputSide.rotateYClockwise();
            final Direction rightDirection = outputSide.rotateYCounterclockwise();
            final Supplier<@Nullable ConveyorLike> output0ConveyorLikeCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, leftDirection, pos.offset(rightDirection), world);
            final Supplier<@Nullable Conveyor> output0ConveyorCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, leftDirection, pos.offset(rightDirection), world);
            final Supplier<@Nullable ConveyorLike> output1ConveyorLikeCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, rightDirection, pos.offset(leftDirection), world);
            final Supplier<@Nullable Conveyor> output1ConveyorCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, rightDirection, pos.offset(leftDirection), world);
            final Supplier<@Nullable ConveyorLike> output2ConveyorLikeCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_LIKE_BLOCK_API_LOOKUP, outputSide, pos.offset(outputDirection), world);
            final Supplier<@Nullable Conveyor> output2ConveyorCache = ConveyorOrientation.createOutputFinder(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, outputSide, pos.offset(outputDirection), world);
            conveyor.conveyor.setup(inputConveyorLikeCache, output0ConveyorLikeCache, output0ConveyorCache, output1ConveyorLikeCache, output1ConveyorCache, output2ConveyorLikeCache, output2ConveyorCache);
            conveyor.initialized = true;
        }
        conveyor.conveyor.tick(TLM.getTickOrder());
        if(conveyor.conveyor.isSyncNeeded()) {
            conveyor.markDirty();
        }
    }

    public enum Choice {
        LEFT,
        RIGHT,
        STRAIGHT,
        RANDOM
    }
}
