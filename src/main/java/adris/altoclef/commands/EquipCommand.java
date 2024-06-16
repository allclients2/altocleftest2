package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;

public class EquipCommand extends Command {
    public EquipCommand() throws CommandException {
        super("equip", "Equips items", new Arg<>(ItemList.class, "[equippable_items]"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        ItemTarget[] items;
        if (parser.getArgUnits().length == 1) {
            switch (parser.getArgUnits()[0].toLowerCase()) { //Hot commands for the default full armor sets
                case "leather" -> items = ItemTarget.of(ItemHelper.LEATHER_ARMORS);
                case "iron" -> items = ItemTarget.of(ItemHelper.IRON_ARMORS);
                case "gold" -> items = ItemTarget.of(ItemHelper.GOLDEN_ARMORS);
                case "diamond" -> items = ItemTarget.of(ItemHelper.DIAMOND_ARMORS);
                case "netherite" -> items = ItemTarget.of(ItemHelper.NETHERITE_ARMORS);
                default -> items = parser.get(ItemList.class).items; // if only one thing was provided, and it isn't an armor set, try to work it out.
            }
        } else {
            items = parser.get(ItemList.class).items; // a list of items was provided
        }

        for (ItemTarget target : items) {
            for (Item item : target.getMatches()) {
                if (!(item instanceof Equipment)) {
                    throw new CommandException("'"+item.toString().toUpperCase() + "' cannot be equipped!");
                }
            }
        }

        mod.runUserTask(new EquipArmorTask(items), this::finish);
    }
}
