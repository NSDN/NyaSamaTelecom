package club.nsdn.nyasamatelecom.api.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Ported from the 1.12.2 {@code NetworkRegister}.
 *
 * <p>1.12.2 registered each message with a numeric <b>discriminator</b> and a {@code Side}:
 * {@code wrapper.registerMessage(handlerClass, messageClass, discriminator, Side.SERVER)}. That
 * integer bookkeeping -- and the {@code discriminatorStart} parameter the mods threaded through their
 * initialisation to avoid collisions -- is gone in NeoForge: a payload is identified by its
 * {@code Type} (a namespaced id) and its direction is implied by whether it is registered
 * {@code playToServer} or {@code playToClient}. So this method loses both the wrapper and the counter,
 * and returns nothing.</p>
 *
 * <p>The {@code Logger} parameter also disappears: payload handlers no longer need a logger injected
 * into a static field, because failures surface through the codec/handler contract instead of a
 * swallowed exception.</p>
 */
@EventBusSubscriber(modid = "nyasamatelecom")
public final class NetworkRegister {

    /** Bump when a payload layout changes incompatibly. */
    public static final String PROTOCOL_VERSION = "3";

    private NetworkRegister() {
    }

    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // 1.12.2: NGTPacket was Side.SERVER (client -> server), ParticlePacket Side.CLIENT.
        registrar.playToServer(NGTPacket.TYPE, NGTPacket.STREAM_CODEC, NGTPacket::handle);
        registrar.playToClient(ParticlePacket.TYPE, ParticlePacket.STREAM_CODEC, ParticlePacket::handle);
    }
}
