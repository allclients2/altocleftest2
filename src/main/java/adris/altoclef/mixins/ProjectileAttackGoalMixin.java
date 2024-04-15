package adris.altoclef.mixins;


import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.chains.MobDefenseChain;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import org.lwjgl.openal.AL;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileAttackGoal.class)
public class ProjectileAttackGoalMixin {


    @Shadow private int updateCountdownTicks;

    @Shadow @Final private MobEntity mob;

    @Inject(method = "tick", at = @At("HEAD"))
    public void inject(CallbackInfo ci) {
        if (updateCountdownTicks < 7 && AltoClef.INSTANCE.getTaskRunner().getCurrentTaskChain() != null ) {
            AltoClef.INSTANCE.getMobDefenseChain().hostileAboutToShoot(mob);
        }
    }

}
