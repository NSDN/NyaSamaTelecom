package club.nsdn.nyasamatelecom.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.opengl.Display;

/**
 * Created by drzzm on 2017.1.12.
 */
public class Utility {

    @SideOnly(Side.CLIENT)
    public static void setTitle() {
        String prevTitle = Display.getTitle();
        if (!prevTitle.contains("NSDN-MC")) {
            Display.setTitle(prevTitle + " | using mods by NSDN-MC");
        }
    }

    public static void say(EntityPlayer player, String format, Object... args) {
        if (player == null) return;
        player.addChatComponentMessage(new ChatComponentTranslation(format, args));
    }

}
