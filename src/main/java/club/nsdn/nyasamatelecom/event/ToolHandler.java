package club.nsdn.nyasamatelecom.event;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Ported from the 1.12.2 {@code ToolHandler}.
 *
 * <p><b>The body is empty in both versions.</b> 1.12.2 registered it on the game bus
 * ({@code MinecraftForge.EVENT_BUS.register(ToolHandler.instance())} from
 * {@code EventRegister.registerCommon()}) with a single {@code onEntityInteract(PlayerInteractEvent
 * .EntityInteract)} that did nothing; the event type still exists in NeoForge unchanged, so the handler is
 * transcribed as the same empty subscriber. It is kept rather than deleted because "the class exists and
 * listens for nothing" is what the 1.12.2 module actually shipped, and because the module's tool behaviour
 * (see {@code api.tool.Connector} / {@code api.tool.DevEditor}) is where that empty hook was meant to go.</p>
 */
@EventBusSubscriber(modid = NyaSamaTelecom.MODID)
public final class ToolHandler {

    private ToolHandler() {
    }

    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Empty in 1.12.2 as well; see the class javadoc.
    }
}
