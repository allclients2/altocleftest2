package adris.altoclef.multiversion;

import adris.altoclef.AltoClef;

public class BaritoneVer {

    @Deprecated
    public static boolean isCanWalkOnEndPortal() {
        return isCanWalkOnEndPortal(AltoClef.INSTANCE);
    }


    public static boolean isCanWalkOnEndPortal(AltoClef mod) {
        //FIXME: Any replacement for lower versions?
        //#if MC >= 12000
        return mod.getExtraBaritoneSettings().isCanWalkOnEndPortal();
        //#else
        //$$ return true;
        //#endif
    }

    @Deprecated
    public static void canWalkOnEndPortal(boolean value) {
        canWalkOnEndPortal(AltoClef.INSTANCE, value);
    }

    public static void canWalkOnEndPortal(AltoClef mod, boolean value) {
        //FIXME: Any replacement for lower versions?
        //#if MC >= 12000
        mod.getExtraBaritoneSettings().canWalkOnEndPortal(value);
        //#endif
    }
}
