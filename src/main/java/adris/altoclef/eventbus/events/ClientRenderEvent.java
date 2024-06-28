package adris.altoclef.eventbus.events;

//#if MC>=11904
import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.gui.DrawableHelper;
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

public class ClientRenderEvent {

    //#if MC>=11904
    public DrawContext context;
    public float tickDelta;

    public ClientRenderEvent(DrawContext context, float tickDelta) {
        this.context = context;
        this.tickDelta = tickDelta;
    }
    //#else
    //$$  public DrawableHelper context;
    //$$  public float tickDelta;
    //$$  public MatrixStack matrices;
    //$$
    //$$  public ClientRenderEvent(DrawableHelper context, MatrixStack matrices, float tickDelta) {
    //$$      this.context = context;
    //$$      this.tickDelta = tickDelta;
    //$$      this.matrices = matrices;
    //$$  }
    //#endif
}
