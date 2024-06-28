package adris.altoclef.multiversion;

import adris.altoclef.AltoClef;
import net.minecraft.client.MinecraftClient;

import java.util.Objects;

public class ClientChatSend {

    public static void sendChatMessage(AltoClef mod, String message) {
        //#if MC >= 11901
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).sendChatMessage(message);
        //#else
        //$$ return mod.getPlayer().sendChatMessage(message);
        //#endif
    }

    public static void sendCommand(AltoClef mod, String command) {
        //#if MC >= 11901
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).sendChatCommand(command);
        //#else
        //$$ return mod.getPlayer().sendChatMessage(command); // Don't know what else.
        //#endif
    }

}
