package adris.altoclef.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 11904
import net.minecraft.network.encryption.PlayerPublicKey;
//#endif

@Mixin(ClientPlayerEntity.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayerEntity {

    //#if MC >= 11904
    public MixinLocalPlayer(ClientWorld world, GameProfile profile, PlayerPublicKey publicKey) {
        super(world, profile);
    }
    //#else
    //$$ public MixinLocalPlayer(ClientWorld world, GameProfile profile) {
    //$$      super(world, profile);
    //$$ }
    //#endif

    @Inject(method = "getPitch", at = @At("RETURN"), cancellable = true)
    public void getPitch(float tickDelta, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(super.getPitch(tickDelta));
    }

    @Inject(method = "getYaw", at = @At("RETURN"), cancellable = true)
    public void getYaw(float tickDelta, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(super.getYaw(tickDelta));
    }
}