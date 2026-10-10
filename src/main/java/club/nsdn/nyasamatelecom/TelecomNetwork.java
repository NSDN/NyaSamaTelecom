package club.nsdn.nyasamatelecom;

import club.nsdn.nyasamatelecom.api.network.NetworkRegister;
import club.nsdn.nyasamatelecom.network.NSPGAEditorHandler;
import club.nsdn.nyasamatelecom.network.NSPGAPacket;
import club.nsdn.nyasamatelecom.network.NSPGAPacketHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Payload registration for the module's own {@code network} package, replacing the NSPGA half of the
 * 1.12.2 {@code network.NetworkWrapper}.
 *
 * <h2>Why this class exists at all</h2>
 * <p>1.12.2 had a single {@code NetworkWrapper} that created the channel and registered every message on
 * it, including the two NSPGA registrations. In the port, {@code api.network.NetworkRegister} already owns
 * the payload registrar for the three {@code api} payloads, and {@code api/**} is frozen (it is a finished
 * deliverable of an earlier wave), so the two {@code network} payloads need their own registrar rather
 * than an edit to that class. {@code RegisterPayloadHandlersEvent#registrar(String)} constructs a brand new
 * {@code PayloadRegistrar} on every call (verified with {@code javap -c}), so a second registrar for the
 * same mod is legal; it uses a distinct version tag so the two sets can be versioned independently.</p>
 *
 * <p>The 1.12.2 shape is preserved in substance: {@link NSPGAPacket} travels in <b>both</b> directions,
 * client -&gt; server handled by {@link NSPGAPacketHandler} and server -&gt; client by
 * {@link NSPGAEditorHandler}, which is what {@code NetworkWrapper} did with two discriminators and two
 * {@code Side}s. <b>The two directions cannot be expressed as two registrations</b>, though -- NeoForge
 * keys its payload registry by (protocol, payload id) and not by flow, so the second registration of the
 * same {@code Type} throws; the two directions are one {@code playBidirectional} registration whose
 * handler dispatches on {@code IPayloadContext#flow}. See {@link #onRegisterPayloads} for the
 * jar-derived evidence and {@code docs/13-WAVE-BRIEFS.md} section 46.5's sibling rule (assert wiring
 * against what the runtime actually keys on, not against the shape the source used to have).</p>
 *
 * <p>{@code NetworkWrapper} itself is <b>not</b> ported: it only existed to hold the
 * {@code SimpleNetworkWrapper} channel and to thread the discriminator counter
 * ({@code NetworkRegister.register(logger, instance, pos)} returning the next free index). Neither concept
 * survives, so it is replaced by this class plus {@code api.network.NetworkRegister} rather than ported as
 * an empty shell.</p>
 */
@EventBusSubscriber(modid = NyaSamaTelecom.MODID)
public final class TelecomNetwork {

    /** Independently versioned from {@link NetworkRegister#PROTOCOL_VERSION}; see the class note. */
    public static final String PROTOCOL_VERSION = NetworkRegister.PROTOCOL_VERSION + "-nspga";

    private TelecomNetwork() {
    }

    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // 1.12.2 registered this one message twice on the channel -- NSPGAPacketHandler as
        // discriminator `pos` / Side.SERVER and NSPGAEditorHandler as `pos + 1` / Side.CLIENT.
        //
        // That cannot be transcribed as two registrations. NeoForge keys its payload registry by
        // (protocol, payload id), NOT by flow -- read out of the jar, not remembered:
        //   NetworkRegistry.PAYLOAD_REGISTRATIONS
        //       : Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>>   (javap -p)
        // and its duplicate check is `byProtocol.containsKey(type.id())`
        // (NetworkRegistry.java:176). So playToServer followed by playToClient on the same Type throws
        // at the second call:
        //   UnsupportedOperationException: Cannot register payload nyasamatelecom:nspga as it is
        //   already registered.
        //   <- crash-2026-10-10_21.54.58-fml.txt, the ninth runClient crash
        //
        // The NeoForge idiom for "one payload, two directions, one handler per side" is a single
        // bidirectional registration whose handler dispatches on IPayloadContext#flow -- i.e. the
        // 1.12.2 Side argument moved from the registration into the handler. One registration per
        // direction, the same two handlers, the same wire layout.
        registrar.playBidirectional(NSPGAPacket.TYPE, NSPGAPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<NSPGAPacket>(
                        // server -> client: 1.12.2 registerMessage(NSPGAEditorHandler.class, ..., pos + 1, Side.CLIENT)
                        NSPGAEditorHandler::handle,
                        // client -> server: 1.12.2 registerMessage(NSPGAPacketHandler.class, ..., pos, Side.SERVER)
                        NSPGAPacketHandler::handle));
    }
}
