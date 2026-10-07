package club.nsdn.nyasamatelecom.api.network;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.api.tool.util.GuiNGTablet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only half of {@link NGTEditorPacket}: finds the held tablet and opens the in-game editor with
 * the received code.
 *
 * <p>Mirrors the lookup {@code /ngt} does server-side (offhand tablet wins, otherwise main hand) --
 * the client has the same item stacks, so nothing else needs to be sent.</p>
 */
final class NGTEditorClient {

    private NGTEditorClient() {
    }

    static void openEditor(String code) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof NGTablet) {
            stack = player.getOffhandItem();
        }
        if (!(stack.getItem() instanceof NGTablet tablet)) {
            return;
        }

        GuiNGTablet screen = new GuiNGTablet(tablet.getModId(), "textures/item/ngt_gui.png", stack);
        screen.setCode(code);
        minecraft.setScreen(screen);
    }
}
