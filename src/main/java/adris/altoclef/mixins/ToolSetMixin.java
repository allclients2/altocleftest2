package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.StorageHelper;
import baritone.Baritone;
import baritone.api.Settings;
import baritone.utils.ToolSet;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Mixin(ToolSet.class)
public class ToolSetMixin {


    @Unique
    private static final Settings.Setting<Boolean> trueSetting;

    // create a setting that is always true using reflection
    static {
        Constructor<?> constructor = Settings.Setting.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        Settings.Setting<Boolean> instance;
        try {
            instance = (Settings.Setting<Boolean>) constructor.newInstance(Baritone.settings(),true);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        trueSetting =  instance;
    }



    @Redirect(method = "getBestSlot(Lnet/minecraft/block/Block;ZZ)I",at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getDamage()I"))
    public int redirected(ItemStack stack,Block block) {
        if (StorageHelper.shouldSaveStack(AltoClef.INSTANCE,block,stack)) {
            return 100_000;
        }

        return stack.getDamage();
    }

    @Redirect(method = "getBestSlot(Lnet/minecraft/block/Block;ZZ)I",at = @At(value = "FIELD", target = "Lbaritone/api/Settings;itemSaver:Lbaritone/api/Settings$Setting;"))
    public Settings.Setting<Boolean> redirected(Settings instance,Block block ,@Local ItemStack stack) {
    //    if (instance.itemSaver.value) return instance.itemSaver;

        if (StorageHelper.shouldSaveStack(AltoClef.INSTANCE,block,stack)) {
            return trueSetting;
        }
        return instance.itemSaver;
    }

}
