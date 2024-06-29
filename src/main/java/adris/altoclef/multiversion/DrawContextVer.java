package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;

// Must be recreated every render event!
public final class DrawContextVer {

    //  Minecraft Colors used here are in a 8-digit hexadecimal format of ARGB (Alpha, Red, Green, Blue)

    // Example with 0xFFDDDDDD
    // Can be Separated to 5 sections: 0x FF DD DD DD

    //    0x: Indicates this is a hexadecimal
    //    FF: is the Alpha channel (opacity)
    //    DD: is the Red channel
    //    DD: is the Green channel
    //    DD: is the Blue channel

    // FF is 255 of the max 255 for 2 hexadecimal digits, this is used in the Alpha channel.
    // DD is 0xDD which is 211, out of the max 255 (0xFF), for all channels. 211 / 255 = 0.827 * 100 = 82.7%
    // This means 0xFFDDDDDD color is fully opaque, and about 82.7% in all channels, making it bright white.

    private final MatrixStack matrices;

    //#if MC>=11903
    private final DrawContext context;
    //#else
    //$$ private final DrawableHelper drawableHelper;
    //#endif

    //#if MC>=11903
    public DrawContextVer(DrawContext context, MatrixStack matrices) {
        this.context = context;
        this.matrices = matrices;
    }
    //#else
    //$$ public DrawContextVer(DrawableHelper drawableHelper, MatrixStack matrices) {
    //$$     this.drawableHelper = drawableHelper;
    //$$     this.matrices = matrices;
    //$$ }
    //#endif


    public void fill(int x1, int y1, int x2, int y2, int color) {
        //#if MC>=11903
        context.fill(RenderLayer.getGuiOverlay(), x1, y1, x2, y2, color);
        //#else
        //$$ DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
        //#endif
    }

    public void drawVerticalLine(int x1, int y1, int y2, int color) {
        //#if MC>=11903
        context.drawVerticalLine(RenderLayer.getGuiOverlay(), x1, y1, y2, color);
        //#else
        //$$ if (y2 < y1) {
        //$$     int i = y1;
        //$$     y1 = y2;
        //$$     y2 = i;
        //$$ }
        //$$ DrawableHelper.fill(matrices, x1, y1 + 1, x1 + 1, y2, color);
        //#endif
    }

    public void drawHorizontalLine(int x1, int x2, int y1, int color) {
        //#if MC>=11903
        context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x1, x2, y1, color);
        //#else
        //$$ if (x2 < x1) {
        //$$     int i = x1;
        //$$     x1 = x2;
        //$$     x2 = i;
        //$$ }
        //$$ drawableHelper.fill(matrices, x1, y1, x2 + 1, y1 + 1, color);
        //#endif
    }

    public void drawText(String text, int x, int y, int color, boolean shadow) {
        //#if MC>=11903
        context.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, color, shadow);
        //#else
        //$$ MinecraftClient.getInstance().textRenderer.draw(matrices, text, x, y, color);
        //#endif
    }

    public MatrixStack getMatrices() {
        return this.matrices;
    }

}


