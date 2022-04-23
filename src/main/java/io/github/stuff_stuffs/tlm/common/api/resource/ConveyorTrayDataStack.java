package io.github.stuff_stuffs.tlm.common.api.resource;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.nbt.NbtCompound;

import java.util.Arrays;

public final class ConveyorTrayDataStack {
    private static final int MAX_STACK_HEIGHT = 32;
    private static final int BITS_PER_STATE;
    private static final State[] STATES;
    private final long[] stack;
    private int stackPointer;

    public ConveyorTrayDataStack() {
        stack = new long[MAX_STACK_HEIGHT];
        stackPointer = -1;
    }

    public boolean isEmpty() {
        return stackPointer == -1;
    }

    public boolean isFull() {
        return stackPointer == MAX_STACK_HEIGHT * (Long.SIZE / BITS_PER_STATE) - 1;
    }

    public State peek() {
        if (isEmpty()) {
            throw new RuntimeException("Cannot peek an empty stack");
        }
        final long frame = stack[stackPointer / 32];
        return select(frame, stackPointer % 32);
    }

    public State pop() {
        if (isEmpty()) {
            throw new RuntimeException("Cannot peek an empty stack");
        }
        final long frame = stack[stackPointer / 32];
        final State state = select(frame, stackPointer % 32);
        stackPointer--;
        return state;
    }

    public void push(final State state) {
        if (isFull()) {
            throw new RuntimeException("Cannot push to full stack");
        }
        stackPointer++;
        final int frameIndex = stackPointer / 32;
        final long frame = stack[frameIndex];
        final int index = stackPointer % 32;
        stack[frameIndex] = store(frame, index, state);
    }

    public NbtCompound writeToNbt() {
        final NbtCompound compound = new NbtCompound();
        compound.putInt("pointer", stackPointer);
        compound.putLongArray("stack", Arrays.copyOf(stack, (stackPointer + 31) / 32));
        return compound;
    }

    public void readFromNbt(final NbtCompound nbt) {
        stackPointer = nbt.getInt("pointer");
        final long[] arr = nbt.getLongArray("stack");
        System.arraycopy(arr, 0, stack, 0, arr.length);
    }

    public NbtCompound writeToClientNbt() {
        final NbtCompound compound = new NbtCompound();
        compound.putInt("pointer", stackPointer);
        final int frame = stackPointer / 32;
        compound.putLong("stackTop", stack[frame]);
        if (frame > 0) {
            compound.putLong("stackNext", stack[frame - 1]);
        }
        return compound;
    }

    public void readFromClientNbt(final NbtCompound nbt) {
        stackPointer = nbt.getInt("pointer");
        final int frame = stackPointer / 32;
        Arrays.fill(stack, 0, frame + 1, 0);
        stack[frame] = nbt.getLong("stackTop");
        if (frame > 0 && nbt.contains("stackNext")) {
            stack[frame - 1] = nbt.getLong("stackNext");
        }
    }

    private static State select(final long frame, final int index) {
        Preconditions.checkArgument(0 <= index, "Cannot have a negative frame index");
        Preconditions.checkArgument(index < 32, "Cannot have a frame index larger than 32");
        final int shift = index * BITS_PER_STATE;
        final long mask = (1L << BITS_PER_STATE) - 1L;
        return STATES[(int) ((frame >>> shift) & mask)];
    }

    private static long store(long frame, final int index, final State state) {
        final int shift = index * BITS_PER_STATE;
        final long mask = (1L << BITS_PER_STATE) - 1L << shift;
        frame &= mask;
        frame |= ((long) state.idx) << shift;
        return frame;
    }

    public enum State {
        RED(0, 0xFFFF0000),
        YELLOW(1, 0xFFFFFF00),
        GREEN(2, 0xFF00FF00),
        BLUE(3, 0xFF0000FF);
        public final int idx;
        public final int color;

        State(final int idx, final int color) {
            this.idx = idx;
            this.color = color;
            Holder.STATES_BY_IDX.put(idx, this);
        }

        public static State getByIdx(final int idx) {
            final State state = Holder.STATES_BY_IDX.get(idx);
            if (state == null) {
                throw new RuntimeException("Invalid state idx: " + idx);
            }
            return state;
        }

        private static final class Holder {
            private static final Int2ReferenceMap<State> STATES_BY_IDX = new Int2ReferenceOpenHashMap<>();
        }
    }

    static {
        final State[] values = State.values();
        if ((values.length & values.length - 1) != 0) {
            throw new RuntimeException("State bits must be a power of 2");
        }
        STATES = values;
        BITS_PER_STATE = Integer.SIZE - Integer.numberOfLeadingZeros(values.length);
    }
}
