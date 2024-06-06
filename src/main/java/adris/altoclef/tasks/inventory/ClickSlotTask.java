package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.slots.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ClickSlotTask extends Task {

    private final Slot slot;
    private final int mouseButton;
    private final SlotActionType type;

    private boolean _clicked = false;

    public ClickSlotTask(Slot slot, int mouseButton, SlotActionType type) {
        this.slot = slot;
        this.mouseButton = mouseButton;
        this.type = type;
    }

    public ClickSlotTask(Slot slot, SlotActionType type) {
        this(slot, 0, type);
    }

    public ClickSlotTask(Slot slot, int mouseButton) {
        this(slot, mouseButton, SlotActionType.PICKUP);
    }

    public ClickSlotTask(Slot slot) {
        this(slot, SlotActionType.PICKUP);
    }

    @Override
    protected void onStart(AltoClef mod) {
        _clicked = false;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getSlotHandler().canDoSlotAction()) {
            mod.getSlotHandler().clickSlot(slot, mouseButton, type);
            mod.getSlotHandler().registerSlotAction();
            _clicked = true;
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task obj) {
        if (obj instanceof ClickSlotTask task) {
            return task.mouseButton == mouseButton && task.type == type && task.slot.equals(slot);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Clicking " + slot.toString();
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return _clicked;
    }
}
