package adris.altoclef.multiversion;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LivingEntityVer {

    public LivingEntityVer() {}

    // FIXME this should be possible with mappings, right?
    @Pattern
    public static Iterable<ItemStack> getItemsEquipped(LivingEntity entity) {
        // Wait these are the same methods?, Oh never mind they are barely different.
        //#if MC >= 12005
        return entity.getEquippedItems();
        //#else
        //$$ return entity.getItemsEquipped();
        //#endif
    }

    @Pattern
    public boolean isSuitableFor(Item item, BlockState state) {
        //#if MC >= 12005
        return item.getDefaultStack().isSuitableFor(state);
        //#else
        //$$ return item.isSuitableFor(state);
        //#endif
    }

    @Pattern
    public static boolean isInAttackRange(MobEntity entity, LivingEntity victim) {
        //#if MC >= 12001
        return entity.isInAttackRange(victim);
        //#else
        //$$ return entity.isInRange(victim, Math.sqrt(entity.squaredAttackRange(victim)));
        //#endif
    }
}