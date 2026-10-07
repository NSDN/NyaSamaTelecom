package club.nsdn.nyasamatelecom.api.tool.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only gateway for opening the NGT tablet editor.
 *
 * <p>Exist for the same reason as the particle packet's client half: the class that decides to open a
 * screen ({@code NGTablet}) is loaded on both sides, so the {@link Minecraft} reference is kept here,
 * where it can only be reached from a client-side branch.</p>
 */
public final class NGTabletClient {

    private NGTabletClient() {
    }

    /** 1.12.2: {@code Minecraft.getMinecraft().displayGuiScreen(new GuiNGTablet(...))}. */
    public static void openEditor(String modid, ItemStack stack) {
        Minecraft.getInstance().setScreen(new GuiNGTablet(modid, "textures/item/ngt_gui.png", stack));
    }
}
