package adris.altoclef.mixins;

import adris.altoclef.util.DumbClass;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.MinecraftClient;

//FIXME: Check version.
//#if MC>=12000
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.option.SimpleOption;

@Mixin(SimpleOption.class)
public class SimpleOptionMixin<T> {
    @Shadow
    T value;

    @Inject(method = "setValue",at = @At("HEAD"), cancellable = true)
    public void inject(T value, CallbackInfo ci) {
        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().options == null) return;
        if (((Object)this) == MinecraftClient.getInstance().options.getGamma()) {
            this.value = value;
            ci.cancel();
        }
    }

}
//#else
//$$ @Mixin(DumbClass.class)
//$$ public class SimpleOptionMixin {}
//#endif

//FIXME: The mixing target does not exist in 1.18.2, nor is it needed. No idea how to get rid of it.
// So i just set the target to a dumb useless class (DumbClass)
