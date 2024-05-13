package adris.altoclef.multiversion;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.world.World;

public class MethodWrapper {



    public static Entity getRenderedEntity(MobSpawnerLogic logic, World world, BlockPos pos) {
        //#if MC>12002
        return logic.getRenderedEntity(world, pos);
        //#else
        //$$ return logic.getRenderedEntity(world,Random.create() ,pos);
        //#endif
    }



}
