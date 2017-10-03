package club.nsdn.nyasamatelecom.GUI;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * Created by drzzm on 2017.3.9.
 */
public class GuiPause extends GuiScreen {

    private Runnable callback;

    public GuiPause() {
        super();
        this.callback = null;
    }

    public GuiPause(Runnable callback) {
        super();
        this.callback = callback;
    }

    @Override
    public void drawScreen(int x, int y, float f)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "JavaFX Running...", this.width / 2, (this.height - this.fontRendererObj.FONT_HEIGHT) / 2, 0xFFFFFF);
        super.drawScreen(x, y, f);
    }

    @Override
    public void onGuiClosed() {
        if (callback != null) callback.run();
    }

}
