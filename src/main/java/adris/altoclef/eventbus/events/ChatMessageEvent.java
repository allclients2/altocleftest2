package adris.altoclef.eventbus.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.message.MessageType;

/**
 * Whenever chat appears
 */
public class ChatMessageEvent {
    private final String messageContent;
    private final GameProfile sender;

    public ChatMessageEvent(String messageContent, GameProfile sender) {
        this.messageContent = messageContent;
        this.sender = sender;
    }

    public String messageContent() {
        return messageContent;
    }
    public String senderName() {
        return sender.getName();
    }
    public GameProfile senderProfile() {
        return sender;
    }
}