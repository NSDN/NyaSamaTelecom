package club.nsdn.nyasamatelecom.Event;

import net.minecraftforge.common.MinecraftForge;

/**
 * Created by drzzm32 on 2016.10.8.
 */

public class EventRegister {

    public static void registerCommon() {
        MinecraftForge.EVENT_BUS.register(ToolHandler.instance());
    }

    public static void registerServer() {

    }

    public static void registerClient() {
        ;
    }

}
