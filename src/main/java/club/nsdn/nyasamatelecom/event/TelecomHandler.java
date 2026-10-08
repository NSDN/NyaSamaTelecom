package club.nsdn.nyasamatelecom.event;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Drives {@link TelecomProcessor} once per server tick, ported from the 1.12.2 {@code TelecomHandler}.
 *
 * <p>1.12.2 registered this class manually (from {@code EventRegister.registerCommon()}, via
 * {@code MinecraftForge.EVENT_BUS.register(TelecomHandler.instance())}) and the singleton
 * {@code instance()} existed only so that registration could hand over an object. In NeoForge the
 * annotation does the registering and handler methods must be {@code static}, so both the singleton and the
 * manual registration disappear -- but the tick body, which is the part that matters, is unchanged.</p>
 *
 * <p>{@code TickEvent.ServerTickEvent} split into {@code Pre}/{@code Post}; the old handler ran on both,
 * and the processor's own {@code tryLock} already makes a double call per tick harmless. {@code Post} is
 * used because the processor reads the states the devices wrote during the tick, so running after them is
 * the closer match to the 1.12.2 ordering.</p>
 */
@EventBusSubscriber(modid = NyaSamaTelecom.MODID)
public final class TelecomHandler {

    private TelecomHandler() {
    }

    @SubscribeEvent
    static void tick(ServerTickEvent.Post event) {
        TelecomProcessor.instance().update();
    }
}
