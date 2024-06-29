package adris.altoclef.multiversion;

import adris.altoclef.Debug;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

//#if MC >= 12001
import org.joml.Matrix4f;
//#else
//$$ import net.minecraft.util.math.Matrix4f;
//#endif

import java.awt.*;

// TextRenderer.TextLayerType.SEE_THROUGH

public class DrawText {
    public static void draw(MatrixStack matrixStack, TextRenderer renderer, String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        //#if MC >= 12001
        renderer.draw(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, backgroundColor, light);
        //#else
        //$$ if (shadow) {
        //$$     renderer.drawWithShadow(matrixStack, text, x, y, color);
        //$$ } else {
        //$$     renderer.draw(matrixStack, text, x, y, color);
        //$$ }
        //#endif
    }

}
