package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ConnectScreenVer {


    @Pattern
    private static void connect(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, boolean quickPlay) {
        //#if MC >= 12005
        ConnectScreen.connect(screen, client, address, info, quickPlay,null);
        //#elseif MC >= 12001
        //$$ ConnectScreen.connect(screen, client, address, info, quickPlay);
        //#else
        //$$ ConnectScreen.connect(screen, client, address, info);
        //#endif
    }

}
