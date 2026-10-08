package club.nsdn.nyasamatelecom;

import club.nsdn.nyasamatelecom.api.tool.util.NGTCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers telecom's commands.
 *
 * <p><b>Why this class exists.</b> The 1.12.2 module registered {@code /ngt} from its
 * {@code ClientProxy} with {@code ClientCommandHandler.instance.registerCommand(new NGTCommand())}.
 * {@code ClientCommandHandler} no longer exists, and {@link NGTCommand} had been ported with its
 * Brigadier {@code register(CommandDispatcher)} entry point intact but <b>no caller at all</b> — so
 * the command simply did not exist in game. This is the same class of gap the project has hit
 * before (a registration layer that compiles but is never invoked), which is why the reasoning is
 * recorded here rather than left implicit.</p>
 *
 * <p>Registered on the <b>game bus</b> ({@link EventBusSubscriber} defaults to it) because
 * {@code RegisterCommandsEvent} is a game-bus event, and on the <b>server</b> side because the ported
 * command needs a {@code ServerPlayer} and hands the code to the client through
 * {@code NGTEditorPacket}. 1.12.2 could keep it client-only because it opened a desktop window
 * locally; the ported version routes {@code /ngt} to the in-game editor instead, which needs the
 * server round trip.</p>
 */
@EventBusSubscriber(modid = NyaSamaTelecom.MODID)
public final class TelecomCommands {

    private TelecomCommands() {
    }

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        NGTCommand.register(event.getDispatcher());
    }
}
