package adris.altoclef.multiversion;

import net.minecraft.entity.damage.DamageSource;

//#if MC >= 12001
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.entity.damage.DamageTypes;
//#else
//$$ import net.minecraft.entity.damage.DamageSource;
//#endif

public class DamageSourcesVer {

    //#if MC >= 12001
    public static boolean isVoidDamage(DamageSource source) {
        return source.isOf(DamageTypes.OUT_OF_WORLD);
    }
    //#else
    //$$   public static boolean isVoidDamage(DamageSource source) {
    //$$     return source.isOutOfWorld();
    //$$   }
    //#endif


    public static boolean bypassesShield(DamageSource source) {
        //#if MC >= 12001
        return source.isIn(DamageTypeTags.BYPASSES_SHIELD);
        //#else
        //$$ return source.isUnblockable(); //I guess?? See: https://maven.fabricmc.net/docs/yarn-1.18.2+build.4/net/minecraft/entity/damage/DamageSource.html#isUnblockable()
        //#endif
    }

    public static boolean bypassesArmor(DamageSource source) {
        //#if MC >= 12001
        return source.isIn(DamageTypeTags.BYPASSES_ARMOR);
        //#else
        //$$ return source.bypassesArmor();
        //#endif
    }
}
