package club.nsdn.nyasamatelecom.api.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -&gt; client: hand the NGT script text to the client so its in-game editor can show it.
 *
 * <p>This payload exists because of a decision recorded in {@code docs/00-PORT-PLAN.md} appendix B:
 * 1.12.2's {@code /ngt} opened a Swing window, and the code reached that window by being passed to a
 * constructor on the server thread. With the desktop window gone and the in-game editor in its place,
 * the code has to travel to the client, which means a payload -- this one.</p>
 *
 * <p>Only the code travels; the client already knows which tablet is held (item stacks are mirrored
 * client-side), so the editor keeps writing back through {@link NGTPacket} as before.</p>
 */
public record NGTEditorPacket(String code) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NGTEditorPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("nyasamatelecom", "ngt_editor"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NGTEditorPacket> STREAM_CODEC =
            StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8,
                    NGTEditorPacket::code, NGTEditorPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NGTEditorPacket packet, IPayloadContext context) {
        // Dispatch only; the Minecraft reference and the screen live in the client-only class.
        NGTEditorClient.openEditor(packet.code());
    }
}
