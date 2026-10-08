package club.nsdn.nyasamatelecom;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * v3 skeleton entry point (Minecraft 1.21.1 / NeoForge).
 *
 * <p>The 1.12.2 implementation is ported: the {@code api} tree (tile entity / device / render /
 * network / util / tool) lives in this module, and {@link TelecomCommands} registers its
 * commands on the game bus. This entry point therefore only carries the mod id, the logger and
 * the constructor signature.</p>
 */
@Mod(NyaSamaTelecom.MODID)
public class NyaSamaTelecom {

    public static final String MODID = "nyasamatelecom";
    public static final Logger LOGGER = LoggerFactory.getLogger("NyaSamaTelecom");

    public NyaSamaTelecom(IEventBus modBus, ModContainer container) {
        LOGGER.info("[{}] v3 skeleton loaded (NeoForge, MC 1.21.1)", MODID);
    }
}