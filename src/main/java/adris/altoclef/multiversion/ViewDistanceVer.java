package adris.altoclef.multiversion;

import net.minecraft.client.option.GameOptions;

import static net.minecraft.client.MinecraftClient.getInstance;

// NOTE: This is more of an option-ver thing.
public class ViewDistanceVer {

    private static final GameOptions minecraftOptions = getInstance().options;

    public static void setViewDistance(int viewDistance) {
        //FIXME: Don't know which version.
        //#if MC >= 12001
        minecraftOptions.getViewDistance().setValue(viewDistance);
        //#else
        //$$  minecraftOptions.viewDistance = viewDistance;
        //#endif
    }
}
