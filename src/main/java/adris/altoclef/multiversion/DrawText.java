package adris.altoclef.multiversion;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;

//#if MC >= 12001
import org.joml.Matrix4f;
//#else
//$$ import net.minecraft.util.math.Matrix4f;
//#endif

import java.awt.*;

// TextRenderer.TextLayerType.SEE_THROUGH

public class DrawText {
    public static void draw(TextRenderer renderer, String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        //#if MC >= 12001
        renderer.draw(text, x, y, color, true, matrix, vertexConsumers, seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, backgroundColor, light);
        //#else
        //$$ renderer.draw(text, x, y, color, true, matrix, vertexConsumers, seeThrough, 0, 255);
        //#endif
    }

}
