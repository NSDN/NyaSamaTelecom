package club.nsdn.nyasamatelecom.Event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.thewdj.telecom.Processor;

/**
 * Created by drzzm32 on 2016.10.10.
 */

public class TelecomCore extends Processor<String> {
    private static TelecomCore instance;

    public static TelecomCore instance() {
        if (instance == null)
            instance = new TelecomCore();
        return instance;
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        this.tick();
    }

}
