package adris.altoclef.multiversion;

import net.minecraft.text.MutableText;

//FIXME: Check version.
//#if MC>=11900
import net.minecraft.text.Text;
//#else
//$$ import net.minecraft.text.LiteralText;
//#endif

public abstract class LiteralTextVer {

    public static MutableText constructLiteralText(String content) {
        //#if MC>=11900
        return Text.literal(content);
        //#else
        //$$ return new LiteralText(content);
        //#endif
    }
}
