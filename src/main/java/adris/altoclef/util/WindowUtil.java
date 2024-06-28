package adris.altoclef.util;

import net.minecraft.client.MinecraftClient;

public class WindowUtil {
    public static int getScaledWindowWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getWindow().getScaledWidth();
    }

    public static int getScaledWindowHeight() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getWindow().getScaledHeight();
    }
}
