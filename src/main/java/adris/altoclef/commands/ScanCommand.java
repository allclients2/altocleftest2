package adris.altoclef.commands;

import adris.altoclef.AltoClef;

import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.multiversion.RegistriesVer;
import adris.altoclef.util.BlockScanner;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Field;
import java.util.Optional;

public class ScanCommand extends Command {

    public ScanCommand() throws CommandException {
        super("scan", "Locates nearest block", new Arg<>(String.class, "block", "DIRT", 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        final String blockName = parser.get(String.class);
        Block block = null;

        for (Item item : RegistriesVer.itemsRegistry()) {
            final String blockKey = ItemHelper.trimItemName(blockName);
            final Identifier identifier = new Identifier(blockKey);
            if (item.getName().equals(identifier)) {
                block = RegistriesVer.blockRegistry().get(identifier);
            }
        }

        if (block == null) {
            Debug.logWarning("Block specified: \"" + blockName + "\" is not valid.");
            return;
        }

        BlockScanner blockScanner = mod.getBlockScanner();

        Optional<BlockPos> scannedBlockPos = blockScanner.getNearestBlock(block, mod.getPlayer().getPos());

        if (scannedBlockPos.isPresent()) {
            Debug.logInternal("Found! Closest Block Location: " +  scannedBlockPos.get());
        } else {
            Debug.logInternal("No Blocks Found.");
        }
    }

}