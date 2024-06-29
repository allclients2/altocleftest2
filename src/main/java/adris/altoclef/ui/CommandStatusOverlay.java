package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.DrawText;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

//#if MC>=12001
import org.joml.Matrix4f;
//#else
//$$ import net.minecraft.util.math.Matrix4f;
//#endif

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class CommandStatusOverlay {

    private static final float overlayScale = 0.88F;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.from(ZoneOffset.of("+00:00"))); // The date formatter

    public void render(AltoClef mod, MatrixStack matrixstack) {
        List<Task> tasks = Collections.emptyList();
        if (mod.getTaskRunner().getCurrentTaskChain() != null) {
            tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
        }

        matrixstack.push();

        drawTaskChain(MinecraftClient.getInstance().textRenderer, matrixstack, 10, 6,
                matrixstack.peek().getPositionMatrix(),
                MinecraftClient.getInstance().getBufferBuilders().getOutlineVertexConsumers(),
                true, 6, tasks, mod);

        matrixstack.pop();
    }

    private static void drawTaskChain(TextRenderer renderer, MatrixStack matrixStack, float x, float y, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int maxLines, List<Task> tasks, AltoClef mod) {
        int whiteColor = 0xFFFFFFFF;

        //#if MC>=11904
        matrix.scale(overlayScale, overlayScale, overlayScale);
        //#else
        //$$ matrixStack.scale(overlayScale, overlayScale, overlayScale);
        //#endif

        float fontHeight = renderer.fontHeight;
        float addX = 4;
        float addY = fontHeight + 2;

        String headerInfo = mod.getTaskRunner().statusReport;
        String realTime = DATE_TIME_FORMATTER.format(Instant.ofEpochMilli((long) (mod.getUserTaskChain().taskStopwatch.time())));

        DrawText.draw(matrixStack, renderer, headerInfo + ((mod.getModSettings().shouldShowTimer() && mod.getUserTaskChain().isActive()) ? (", timer: " + realTime) : ""), x, y, Color.LIGHT_GRAY.getRGB(), true, matrix, vertexConsumers, seeThrough, 0, 255);
        y += addY;

        if (tasks.isEmpty()) {
            if (mod.getTaskRunner().isActive()) {
                DrawText.draw(matrixStack, renderer, " (no task running) ", x, y, whiteColor, true, matrix, vertexConsumers, seeThrough, 0, 255);
            }
            return;
        }


        if (tasks.size() <= maxLines) {
            for (Task task : tasks) {
                renderTask(task, renderer, matrixStack, x, y, matrix, vertexConsumers, seeThrough);

                x += addX;
                y += addY;
            }
            return;
        }

        // FIXME: Don't think the number of displayed "Other tasks" is accurate...
        for (int i = 0; i < tasks.size(); ++i) {
            if (i == 2) { // So we can see the second top task..
                x += addX * 2;
                DrawText.draw(matrixStack, renderer, "... " + (tasks.size() - maxLines) + " other task(s) ...", x, y, whiteColor, true, matrix, vertexConsumers, seeThrough, 0, 255);
            } else if (i <= 1 || i > (tasks.size() - maxLines + 1)) {
                renderTask(tasks.get(i), renderer, matrixStack, x, y, matrix, vertexConsumers, seeThrough);
            } else {
                continue;
            }

            x += addX;
            y += addY;
        }


    }


    private static void renderTask(Task task, TextRenderer renderer, MatrixStack matrixStack, float x, float y, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough) {
        String taskName = task.getClass().getSimpleName() + " ";
        DrawText.draw(matrixStack, renderer, taskName, x, y, new Color(128, 128, 128).getRGB(), true, matrix, vertexConsumers, seeThrough, 0, 255);

        DrawText.draw(matrixStack, renderer, task.toString(), x + renderer.getWidth(taskName), y, new Color(255, 255, 255).getRGB(), true, matrix, vertexConsumers, seeThrough, 0, 255);

    }

}
