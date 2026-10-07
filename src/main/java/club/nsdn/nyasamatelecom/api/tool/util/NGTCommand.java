package club.nsdn.nyasamatelecom.api.tool.util;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * {@code /ngt} — opens the NGT script editor for the tablet the player is holding.
 * Ported from the 1.12.2 {@code NGTCommand} ({@code ICommand}).
 *
 * <p>Brigadier changes the shell of this command only: {@code getName}/{@code getAliases}/
 * {@code getTabCompletions}/{@code isUsernameIndex}/{@code compareTo} all disappear (the literal name
 * and {@code executes(...)} replace them), {@code checkPermission} returning {@code true} becomes the
 * absence of a {@code requires(...)} predicate, and messages are literal
 * {@link Component}s.</p>
 *
 * <p><b>One deliberate behaviour change, flagged for the author.</b> 1.12.2 opened a Swing
 * ({@code javax.swing.JFrame}) desktop window -- see the note in {@code api/tool/util/NGTEditor}. That
 * is an AWT dependency inside a GLFW/LWJGL game process (and the old code started it on a bare
 * {@code new Thread} from the command, i.e. from the server thread), which is exactly the class of
 * defect the port plan lists as an AWT clean-up. This port therefore routes {@code /ngt} to the
 * <i>in-game</i> editor that already existed alongside it in 1.12.2
 * ({@code GuiNGTablet extends GuiScreen}) instead of resurrecting the desktop window.</p>
 *
 * <p>The tablet validation and code extraction below are a faithful port; only the final
 * "show the editor" step is pending, because it needs the ported {@code GuiNGTablet} and a
 * server-to-client payload to move the code to the client that will display it. That seam is
 * {@link #openEditorOnClient}, so nothing else has to change when it lands.</p>
 */
public final class NGTCommand {

    private NGTCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 1.12.2 checkPermission() returned true for everyone, so no requires(...) predicate is added.
        dispatcher.register(Commands.literal("ngt")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();

                    ItemStack stack = player.getMainHandItem();
                    if (player.getOffhandItem().getItem() instanceof NGTablet) {
                        stack = player.getOffhandItem();
                    }
                    if (!(stack.getItem() instanceof NGTablet)) {
                        player.displayClientMessage(
                                Component.literal("You should held NGTablet!").withStyle(ChatFormatting.DARK_RED),
                                false);
                        return 0;
                    }

                    ListTag list = Util.getTagListFromNGT(stack);
                    String code = list != null ? NSASM.getCodeString(list) : "";
                    openEditorOnClient(player, code);
                    return 1;
                }));
    }

    /**
     * Hands the code to the client that should display the editor.
     *
     * <p>Implemented with {@link club.nsdn.nyasamatelecom.api.network.NGTEditorPacket}: only the code
     * travels, because the client already knows which tablet is held. The 1.12.2 message
     * {@code NGTEditor started} was a symptom of opening a desktop window from the server thread, so
     * it is not reproduced.</p>
     */
    private static void openEditorOnClient(ServerPlayer player, String code) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                player, new club.nsdn.nyasamatelecom.api.network.NGTEditorPacket(code));
    }
}
