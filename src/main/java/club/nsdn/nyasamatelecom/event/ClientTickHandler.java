package club.nsdn.nyasamatelecom.event;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Ported from the 1.12.2 {@code ClientTickHandler}.
 *
 * <p><b>The body is empty in both versions.</b> 1.12.2 registered it from the client proxy
 * ({@code EventRegister.registerClient()}) with an empty {@code tick(TickEvent.ClientTickEvent)}. The
 * registration site moves from the proxy to {@code value = Dist.CLIENT} on the annotation (the proxies
 * themselves are not ported -- see {@code EventRegister}'s javadoc), and the event is the {@code Post}
 * phase now that the old single tick event was split.</p>
 */
@EventBusSubscriber(modid = NyaSamaTelecom.MODID, value = Dist.CLIENT)
public final class ClientTickHandler {

    private ClientTickHandler() {
    }

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        // Empty in 1.12.2 as well; see the class javadoc.
    }
}
