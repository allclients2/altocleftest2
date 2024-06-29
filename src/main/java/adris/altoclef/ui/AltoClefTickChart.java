package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.DrawContextVer;
import adris.altoclef.multiversion.InGameHudVer;
import adris.altoclef.util.WindowUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.math.MathHelper.lerp;

/**
 * displays a chart indicating how much of the tick time is taken up by altoclef
 * (very similar to Minecrafts `TickChart`)
 */
public class AltoClefTickChart {


    protected final TextRenderer textRenderer;
    protected final List<Long> list = new ArrayList<>();

    public AltoClefTickChart(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }


    public void pushTickNanos(long nanoTime) {
        list.add(nanoTime);
    }


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

    //#if MC>=11903
    public void render(AltoClef mod, DrawContext context, int x, int width) {
    //#else
    //$$ public void render(AltoClef mod, DrawableHelper helper, MatrixStack matrices, int x, int width) {
    //#endif

        if (InGameHudVer.shouldShowDebugHud() || !mod.getTaskRunner().isActive()) return;

        //#if MC>=11903
        final DrawContextVer contextVer = new DrawContextVer(context, context.getMatrices());
        //#else
        //$$ final DrawContextVer contextVer = new DrawContextVer(helper, matrices);
        //#endif

        int height = WindowUtil.getScaledWindowHeight();
        contextVer.fill(x, height - 37, x + width, height, 0x90505050);

        long max = Integer.MAX_VALUE;
        long min = Integer.MIN_VALUE;


        while (list.size() >= width - 1) {
            list.remove(0);
        }

        for (int i = 0; i < list.size(); ++i) {
            int p = x + i + 1;

            long r = this.get(i);
            max = Math.min(max, r);
            min = Math.max(min, r);

            this.drawTotalBar(contextVer, p, height, i);
        }

        contextVer.drawHorizontalLine(x, x + width - 1, height - 37, 0xFFDDDDDD);
        contextVer.drawHorizontalLine(x, x + width - 1, height - 1, 0xFFDDDDDD);

        contextVer.drawVerticalLine(x, height - 37, height, 0xFFDDDDDD);
        contextVer.drawVerticalLine(x + width - 1, height - 37, height, 0xFFDDDDDD);


        this.drawBorderedText(contextVer, "50 ms", x + 1, height - 37 + 1);
    }


    protected void drawTotalBar(DrawContextVer context, int x, int y, int index) {
        long l = list.get(index);
        int i = this.getHeight(l);
        int j = this.getColor(l);
        context.fill(x, y - i, x + 1, y, j);
    }

    protected long get(int index) {
        return list.get(index);
    }


    protected void drawBorderedText(DrawContextVer context, String string, int x, int y) {
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(0.5f,0.5f,1);

        context.fill(x*2, y*2, x*2 + this.textRenderer.getWidth(string) + 2, y*2 + this.textRenderer.fontHeight+1, 0x90505050);
        context.drawText(string, (x + 1)*2, (y + 1)*2, 0xE9E9E9, false);

        matrixStack.pop();
    }



    protected int getHeight(double value) {
        return (int)Math.round(nanosToMillis(value) * 37 / 50d);
    }


    protected int getColor(long value) {
        float maxMs = 50f;
        double ms = nanosToMillis(value);

        if (ms > maxMs) {
            return 0xFFFFFFFF;
        }

        // minColor is Green
        // medianColor is Orange
        // maxColor is Red

        return getColor(ms/maxMs, 0xFF00FF00, 0xFFFFC800, 0xFFFF0000);
    }

    protected int getColor(double value, int minColor, int medianColor, int maxColor) {
        if (value < 0.5) { // Hopefully this lerp works...
            return (int) lerp((float)((value) / (0.5)), minColor, medianColor);
        }
        return (int) lerp((float)((value - 0.5) / 0.5), medianColor, maxColor);
    }

    private static double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }

}
