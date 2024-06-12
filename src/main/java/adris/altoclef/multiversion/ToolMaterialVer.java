package adris.altoclef.multiversion;

import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;

public class ToolMaterialVer {

    public static int getMiningLevel(ToolItem item) {
        return getMiningLevel(item.getMaterial());
    }

    public static int getMiningLevel(ToolMaterial material) {
        switch (material) {
            case ToolMaterials.WOOD, ToolMaterials.GOLD -> {
                return 0;
            }
            case ToolMaterials.STONE -> {
                return 1;
            }
            case ToolMaterials.IRON -> {
                return 2;
            }
            case ToolMaterials.DIAMOND -> {
                return 3;
            }
            case ToolMaterials.NETHERITE -> {
                return 4;
            }
            default -> throw new IllegalStateException("Unexpected value: " + material);
        }
    }

}
