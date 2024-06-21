package adris.altoclef.util;

import adris.altoclef.AltoClef;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;

// Helps get the best weapon available.

public abstract class Weapons {

    //Ranked best to worst
    public static final Item[] SWORDS = new Item[]{
            //Swords Of course
            Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.STONE_SWORD,
            Items.WOODEN_SWORD,
    };

    public static final Item[] AXES = new Item[]{
            //Axes can be helpful, but not as much as swords
            Items.NETHERITE_AXE, Items.DIAMOND_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.STONE_AXE,
            Items.WOODEN_AXE,
    };

    public static final class Weapon {
        public Item WeaponItem;
        public int TypeId;

        public Weapon(Item item, int Id) {
            WeaponItem = item;
            TypeId = Id;
        }
    }

    public static Weapon getBestWeapon(AltoClef mod) {
        int WeaponId = 0;

        Item bestWeapon = null;
        float bestDamage = 0.0f;

        for (Item item : SWORDS) {
            final float damage = ((SwordItem) item).getMaterial().getAttackDamage();
            if (mod.getItemStorage().hasItem(item) && damage > bestDamage) {
                bestWeapon = item;
                bestDamage = damage;
                WeaponId = 1;
            }
        }

        for (Item item : AXES)
        {
            final float damage = ((AxeItem) item).getMaterial().getAttackDamage();
            if (mod.getItemStorage().hasItem(item) && (damage * 0.6) > bestDamage) {
                bestWeapon = item;
                bestDamage = damage;
                WeaponId = 2;
            }
        }

        return new Weapon(bestWeapon, WeaponId);
    }

    public static float getBestDamage(Weapon BestWeapon, AltoClef mod) {
        if (BestWeapon.TypeId == 1) {
            return ((SwordItem) BestWeapon.WeaponItem).getMaterial().getAttackDamage();
        } else if (BestWeapon.TypeId == 2) {
            return ((AxeItem) BestWeapon.WeaponItem).getMaterial().getAttackDamage();
        } else {
            //Debug.logInternal("Invalid Weapon Type");
            return 0;
        }
    }

}
