package club.nsdn.nyasamatelecom.network;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client side of the NSPGA flash payload, ported from the 1.12.2 {@code NSPGAEditorHandler}.
 *
 * <h2>NOT PORTED: the editor itself</h2>
 * <p>1.12.2's handler did one thing -- it opened {@code cn.ac.nya.nspga.flex.NSPGAEditor}, a desktop
 * (Swing) window, pre-filled with the device's current input/output names and code, and sent the result
 * back up as another {@link NSPGAPacket}. That class <b>no longer exists in this repository</b>: the
 * {@code core} module's port of {@code cn.ac.nya.nspga.flex} contains only
 * {@code BinUtil}, {@code INSPGAFlex}, {@code NSPGAF011NHV1}, {@code NSPGAF211NHV1} and
 * {@code NSPGAF344NHV1} (verified by directory listing), because a Swing window is out of scope for
 * 1.21.1 -- and {@code javax.swing} is on this port's forbidden list besides.</p>
 *
 * <p>So the handler keeps the half that <i>can</i> be ported and stops there: the packet still travels to
 * the client and is still fully readable (the whole 1.12.2 accessor set is preserved on
 * {@link NSPGAPacket}), and the absence is logged instead of silently pretending an editor opened. Note
 * that the two 1.12.2 messages this class used to send, {@code info.nspga.editor} and
 * {@code info.nspga.flash}, are deliberately <b>not</b> sent -- both would be false statements about a
 * window that does not exist. They remain in the language files, unused, because they are the author's
 * keys and removing them is not this port's call.</p>
 *
 * <p>What a future round needs to do: replace this with in-game editor, exactly as the NGT terminal's
 * Swing window was replaced by {@code api.tool.util.NGTEditor} plus {@code NGTEditorPacket}. That is a new
 * screen, not a translation, which is why it is recorded as a gap rather than guessed at here.</p>
 */
public final class NSPGAEditorHandler {

    private NSPGAEditorHandler() {
    }

    public static void handle(NSPGAPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> NyaSamaTelecom.LOGGER.warn(
                "[{}] received an NSPGA flash request for {} (io={}, in={}, out={}) but the flash editor "
                        + "is NOT PORTED (the Swing NSPGAEditor was dropped with the core module's port); "
                        + "the device was not changed",
                NyaSamaTelecom.MODID, packet.getPos(), packet.ioCount(),
                packet.inputSize(), packet.outputSize()));
    }
}
