package club.nsdn.nyasamatelecom;

import club.nsdn.nyasamatelecom.api.network.NetworkRegister;
import club.nsdn.nyasamatelecom.network.NSPGAEditorHandler;
import club.nsdn.nyasamatelecom.network.NSPGAPacket;
import club.nsdn.nyasamatelecom.network.NSPGAPacketHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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
 * <p>The 1.12.2 shape is preserved exactly: {@link NSPGAPacket} is registered <b>twice</b>, client -&gt;
 * server with {@link NSPGAPacketHandler} and server -&gt; client with {@link NSPGAEditorHandler}, which is
 * what {@code NetworkWrapper} did with two discriminators and two {@code Side}s.</p>
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
        // 1.12.2: registerMessage(NSPGAPacketHandler.class, NSPGAPacket.class, pos, Side.SERVER).
        registrar.playToServer(NSPGAPacket.TYPE, NSPGAPacket.STREAM_CODEC, NSPGAPacketHandler::handle);
        // 1.12.2: registerMessage(NSPGAEditorHandler.class, NSPGAPacket.class, pos + 1, Side.CLIENT).
        registrar.playToClient(NSPGAPacket.TYPE, NSPGAPacket.STREAM_CODEC, NSPGAEditorHandler::handle);
    }
}
