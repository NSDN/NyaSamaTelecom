package club.nsdn.nyasamatelecom.event;

import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Created by drzzm32 on 2017.12.27.
 */
public class TelecomHandler {

    private static TelecomHandler instance;

    public static TelecomHandler instance() {
        if (instance == null)
            instance = new TelecomHandler();
        return instance;
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        TelecomProcessor.instance().update();
    }

}
