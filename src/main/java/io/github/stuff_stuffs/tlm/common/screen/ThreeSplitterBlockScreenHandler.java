package io.github.stuff_stuffs.tlm.common.screen;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTrayDataStack;
import io.github.stuff_stuffs.tlm.common.block.entity.conveyor.ThreeSplitterConveyorBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class ThreeSplitterBlockScreenHandler extends ScreenHandler {
    public static final int CHOICE_RED_BUTTON_ID = 0;
    public static final int CHOICE_RED_POP_BUTTON_ID = 1;
    public static final int CHOICE_GREEN_BUTTON_ID = 2;
    public static final int CHOICE_GREEN_POP_BUTTON_ID = 3;
    public static final int CHOICE_BLUE_BUTTON_ID = 4;
    public static final int CHOICE_BLUE_POP_BUTTON_ID = 5;
    public static final int CHOICE_YELLOW_BUTTON_ID = 6;
    public static final int CHOICE_YELLOW_POP_BUTTON_ID = 7;
    private final ThreeSplitterConveyorBlockEntity entity;
    private final Map<ConveyorTrayDataStack.State, Pair<Property, Property>> propertyMap;
    private final Property emptyProperty;
    private final Property stackProperty;

    public ThreeSplitterBlockScreenHandler(final int syncId) {
        super(TLMScreenHandlerTypes.THREE_SPLITTER_BLOCK_SCREEN_HANDLER_TYPE, syncId);
        entity = null;
        propertyMap = new EnumMap<>(ConveyorTrayDataStack.State.class);
        for (final ConveyorTrayDataStack.State state : ConveyorTrayDataStack.State.values()) {
            final Property stateProperty = new Property() {
                private int val = 0;

                @Override
                public int get() {
                    return val;
                }

                @Override
                public void set(final int value) {
                    val = value;
                }
            };
            final Property popProperty = new Property() {
                private int val = 0;

                @Override
                public int get() {
                    return val;
                }

                @Override
                public void set(final int value) {
                    val = value;
                }
            };
            addProperty(stateProperty);
            addProperty(popProperty);
            propertyMap.put(state, Pair.of(stateProperty, popProperty));
        }
        emptyProperty = new Property() {
            private int val = 0;

            @Override
            public int get() {
                return val;
            }

            @Override
            public void set(final int value) {
                val = value;
            }
        };
        addProperty(emptyProperty);
        stackProperty = new Property() {
            private int val = 0;

            @Override
            public int get() {
                return val;
            }

            @Override
            public void set(final int value) {
                val = value;
            }
        };
        addProperty(stackProperty);
    }

    public ThreeSplitterBlockScreenHandler(final ThreeSplitterConveyorBlockEntity entity, final int syncId) {
        super(TLMScreenHandlerTypes.THREE_SPLITTER_BLOCK_SCREEN_HANDLER_TYPE, syncId);
        this.entity = entity;
        propertyMap = new EnumMap<>(ConveyorTrayDataStack.State.class);
        final ThreeSplitterConveyorBlockEntity.Choice[] choices = ThreeSplitterConveyorBlockEntity.Choice.values();
        for (final ConveyorTrayDataStack.State state : ConveyorTrayDataStack.State.values()) {
            final MutableInt stateMut = new MutableInt();
            final MutableBoolean popMut = new MutableBoolean();
            stateMut.setValue(entity.getChoice(state).ordinal());
            popMut.setValue(entity.getPop(state));
            final Property stateProperty = new Property() {
                @Override
                public int get() {
                    return stateMut.intValue();
                }

                @Override
                public void set(final int value) {
                    final int i = value % choices.length;
                    entity.setChoice(state, choices[i], popMut.booleanValue());
                    stateMut.setValue(i);
                }
            };
            final Property popProperty = new Property() {
                @Override
                public int get() {
                    return entity.getPop(state) ? 1 : 0;
                }

                @Override
                public void set(final int value) {
                    final boolean pop = (value & 1) != 0;
                    entity.setChoice(state, choices[stateMut.intValue()], pop);
                    popMut.setValue(pop);
                }
            };
            addProperty(stateProperty);
            addProperty(popProperty);
            propertyMap.put(state, Pair.of(stateProperty, popProperty));
        }
        emptyProperty = new Property() {
            @Override
            public int get() {
                return entity.getChoice(null).ordinal();
            }

            @Override
            public void set(final int value) {
                entity.setChoice(null, choices[value % choices.length], false);
            }
        };
        addProperty(emptyProperty);
        stackProperty = new Property() {
            @Override
            public int get() {
                return entity.getStack();
            }

            @Override
            public void set(final int value) {
                entity.setStack(value);
            }
        };
        addProperty(stackProperty);
    }

    @Override
    public boolean onButtonClick(final PlayerEntity player, final int id) {
        if (isClient()) {
            MinecraftClient.getInstance().interactionManager.clickButton(syncId, id);
        } else if (isServer()) {
            switch (id) {
                case CHOICE_RED_BUTTON_ID -> {
                    final Property first = propertyMap.get(ConveyorTrayDataStack.State.RED).getFirst();
                    first.set(first.get() + 1);
                }
                case CHOICE_RED_POP_BUTTON_ID -> {
                    final Property second = propertyMap.get(ConveyorTrayDataStack.State.RED).getSecond();
                    second.set(second.get() + 1);
                }
                case CHOICE_GREEN_BUTTON_ID -> {
                    final Property first = propertyMap.get(ConveyorTrayDataStack.State.GREEN).getFirst();
                    first.set(first.get() + 1);
                }
                case CHOICE_GREEN_POP_BUTTON_ID -> {
                    final Property second = propertyMap.get(ConveyorTrayDataStack.State.GREEN).getSecond();
                    second.set(second.get() + 1);
                }
                case CHOICE_BLUE_BUTTON_ID -> {
                    final Property first = propertyMap.get(ConveyorTrayDataStack.State.BLUE).getFirst();
                    first.set(first.get() + 1);
                }
                case CHOICE_BLUE_POP_BUTTON_ID -> {
                    final Property second = propertyMap.get(ConveyorTrayDataStack.State.BLUE).getSecond();
                    second.set(second.get() + 1);
                }
                case CHOICE_YELLOW_BUTTON_ID -> {
                    final Property first = propertyMap.get(ConveyorTrayDataStack.State.YELLOW).getFirst();
                    first.set(first.get() + 1);
                }
                case CHOICE_YELLOW_POP_BUTTON_ID -> {
                    final Property second = propertyMap.get(ConveyorTrayDataStack.State.YELLOW).getSecond();
                    second.set(second.get() + 1);
                }
            }
            sendContentUpdates();
        }
        return true;
    }

    public ThreeSplitterConveyorBlockEntity.Choice getChoice(@Nullable final ConveyorTrayDataStack.State state) {
        final ThreeSplitterConveyorBlockEntity.Choice[] choices = ThreeSplitterConveyorBlockEntity.Choice.values();
        if (state == null) {
            return choices[emptyProperty.get() % choices.length];
        }
        return choices[propertyMap.get(state).getFirst().get() % choices.length];
    }

    public boolean getPop(@Nullable final ConveyorTrayDataStack.State state) {
        if (state == null) {
            return false;
        }
        return (propertyMap.get(state).getSecond().get() & 1) != 0;
    }

    public int getStack() {
        return stackProperty.get();
    }

    @Override
    public boolean canUse(final PlayerEntity player) {
        return true;
    }

    public boolean isClient() {
        return entity == null;
    }

    public boolean isServer() {
        return entity != null;
    }
}
