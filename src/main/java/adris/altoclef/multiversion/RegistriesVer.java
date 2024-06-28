package adris.altoclef.multiversion;

// IN 1.19.3, the Registry class was moved and renamed to Registries. So FROM 1.19.3 AND ONWARDS. The registry class is renamed.

import net.minecraft.block.Block;
import net.minecraft.item.Item;

//#if MC>=11903
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
//#else
//$$ import net.minecraft.util.registry.Registry;
//$$ import net.minecraft.util.registry.DefaultedRegistry;
//#endif

public class RegistriesVer {

    public static DefaultedRegistry<Item> itemsRegistry() {
        //#if MC>=11903
        return Registries.ITEM;
        //#else
        //$$  return Registry.ITEM;
        //#endif
    }

    public static DefaultedRegistry<Block> blockRegistry() {
        //#if MC>=11903
        return Registries.BLOCK;
        //#else
        //$$  return Registry.BLOCK;
        //#endif
    }

}
