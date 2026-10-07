package club.nsdn.nyasamatelecom.api.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Ported from the 1.12.2 {@code ParticlePacket} ({@code IMessage}): asks clients in one dimension to
 * spawn a particle at a position with a velocity.
 *
 * <p>Note the component is called {@code particleType} rather than {@code type}: in 1.21.1
 * {@link CustomPacketPayload#type()} already means "which payload is this", so a record component
 * named {@code type} cannot coexist with the interface method. The 1.12.2 field was simply
 * {@code type}.</p>
 *
 * <p>Two other changes stand out:</p>
 * <ul>
 *   <li>The hand-written {@code toBytes}/{@code fromBytes} pair (which wrote the string length before
 *       the string by hand) is replaced by an explicit {@link StreamCodec}. A seven-field payload
 *       exceeds {@code StreamCodec.composite}'s arity, so the codec is written directly.</li>
 *   <li>{@code SimpleNetworkWrapper#sendToDimension} becomes
 *       {@link PacketDistributor#sendToPlayersInDimension(ServerLevel, CustomPacketPayload)}, which
 *       takes the level rather than a raw dimension id -- the 1.12.2 {@code int dimension} parameter
 *       disappears from the API.</li>
 * </ul>
 *
 * <p>The old {@code ParticlePacketHandler} is folded in as {@link #handle}, which only dispatches to
 * the client-only class: the particle code touches {@code Minecraft}, and anything that references it
 * must never be loaded on a dedicated server.</p>
 */
public record ParticlePacket(String particleType, double x, double y, double z, double tX, double tY, double tZ)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ParticlePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("nyasamatelecom", "particle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticlePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeUtf(packet.particleType());
                buffer.writeDouble(packet.x());
                buffer.writeDouble(packet.y());
                buffer.writeDouble(packet.z());
                buffer.writeDouble(packet.tX());
                buffer.writeDouble(packet.tY());
                buffer.writeDouble(packet.tZ());
            },
            buffer -> new ParticlePacket(
                    buffer.readUtf(),
                    buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                    buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Server -&gt; clients in the given level. */
    public void send(ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, this);
    }

    public static void handle(ParticlePacket packet, IPayloadContext context) {
        ParticlePacketClientHandler.handle(packet);
    }
}
