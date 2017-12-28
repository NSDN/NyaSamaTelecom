package club.nsdn.nyasamatelecom.event;

import club.nsdn.nyasamatelecom.util.Utility;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by drzzm32 on 2017.1.12.
 */
public class ClientTickHandler {
    private static ClientTickHandler instance;

    public static ClientTickHandler instance() {
        if (instance == null)
            instance = new ClientTickHandler();
        return instance;
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        Utility.setTitle();
    }
}
