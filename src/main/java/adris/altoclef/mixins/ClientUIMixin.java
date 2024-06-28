package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.util.math.MatrixStack;


//#if MC>=11904
@Mixin(InGameHud.class)
public final class ClientUIMixin {
//#else
//$$ import net.minecraft.client.gui.DrawableHelper;
//$$
//$$ @Mixin(InGameHud.class)
//$$ public final class ClientUIMixin extends DrawableHelper {
//#endif


    //#if MC>=11904
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void clientRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        EventBus.publish(new ClientRenderEvent(context, tickDelta));
    }
    //#else
    //$$    @Inject(
    //$$            method = "render",
    //$$            at = @At("TAIL")
    //$$    )
    //$$    private void render(MatrixStack matrices, float tickDelta) {
    //$$        EventBus.publish(new ClientRenderEvent(this, matrices, tickDelta));
    //$$    }
    //#endif
}
