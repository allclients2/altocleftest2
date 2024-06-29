package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;

import net.minecraft.network.message.MessageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 12001
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.SignedMessage;

import java.util.UUID;
//#else
//$$ import net.minecraft.network.MessageType;
//$$ import net.minecraft.client.gui.hud.ChatHudListener;
//$$ import net.minecraft.text.Text;
//$$ import java.util.UUID;
//#endif

//#if MC >= 12001
@Mixin(MessageHandler.class)
public final class ChatReadMixin {
//#else
//$$ @Mixin(ChatHudListener.class)
//$$ public class ChatReadMixin {
//#endif

    //#if MC >= 12001
    @Inject(
            method = "onChatMessage",
            at = @At("HEAD")
    )
    private void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        ChatMessageEvent evt = new ChatMessageEvent(message.getContent().getString(), sender);
        EventBus.publish(evt);
    }
    //#else
    //$$  @Inject(
    //$$          method = "onChatMessage",
    //$$          at = @At("HEAD")
    //$$  )
    //$$  private void onChatMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
    //$$      if (senderUuid == null || AltoClef.INSTANCE.getPlayer() == null || senderUuid == AltoClef.INSTANCE.getPlayer().getUuid() || senderUuid.equals(new UUID(0, 0))) return;
    //$$      Debug.logMessage("UUID:" + senderUuid);
    //$$      ChatMessageEvent evt = new ChatMessageEvent(message.getString(), new GameProfile(senderUuid, null));
    //$$      EventBus.publish(evt);
    //$$ }
    //#endif
}
