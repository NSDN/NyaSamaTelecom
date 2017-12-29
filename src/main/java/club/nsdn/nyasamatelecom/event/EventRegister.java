package club.nsdn.nyasamatelecom.event;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class EventRegister {

    public static void registerCommon() {
        MinecraftForge.EVENT_BUS.register(ToolHandler.instance());
    }

    public static void registerServer() {
        FMLCommonHandler.instance().bus().register(ServerTickHandler.instance());
        FMLCommonHandler.instance().bus().register(TelecomProcessor.instance());
    }

    public static void registerClient() { FMLCommonHandler.instance().bus().register(ClientTickHandler.instance()); }

}
