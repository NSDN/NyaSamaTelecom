package club.nsdn.nyasamatelecom.Event;

import club.nsdn.nyasamatelecom.Entity.ITrainLinkable;
import club.nsdn.nyasamatelecom.TrainControl.NetworkWrapper;
import club.nsdn.nyasamatelecom.TrainControl.TrainPacket;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

/**
 * Created by drzzm32 on 2016.6.7.
 */
public class ToolHandler {
    private static ToolHandler instance;

    public static ToolHandler instance() {
        if (instance == null)
            instance = new ToolHandler();
        return instance;
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event) {

    }

}
