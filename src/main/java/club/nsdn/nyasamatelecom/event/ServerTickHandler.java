package club.nsdn.nyasamatelecom.event;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Ported from the 1.12.2 {@code ServerTickHandler}.
 *
 * <p><b>The body is empty in both versions.</b> 1.12.2 registered it only from the <i>server</i> proxy
 * ({@code EventRegister.registerServer()}), and the class carried the warning
 * {@code // NOTE: Dedicated Server ONLY! Single-player will not work!} -- because that proxy is the
 * dedicated-server one. That distinction has no counterpart now: the proxies are gone, {@code ServerTickEvent}
 * fires in single-player too, and there is no "dedicated server only" flag on {@code @EventBusSubscriber}.
 * <b>NOT PORTED: the dedicated-server-only restriction.</b> It is recorded rather than emulated because the
 * handler does nothing in either version, so the restriction only ever decided which jar registered a
 * no-op.</p>
 */
@EventBusSubscriber(modid = NyaSamaTelecom.MODID)
public final class ServerTickHandler {

    private ServerTickHandler() {
    }

    @SubscribeEvent
    static void tick(ServerTickEvent.Post event) {
        // Empty in 1.12.2 as well; see the class javadoc.
    }
}
