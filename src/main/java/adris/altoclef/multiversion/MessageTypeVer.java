package adris.altoclef.multiversion;

import net.minecraft.network.message.MessageType;

public class MessageTypeVer {


    @Pattern
    private static MessageType type(MessageType.Parameters params) {
        //#if MC >= 12006
        return params.type().value();
        //#else
        //$$ return params.type();
        //#endif
    }

}
