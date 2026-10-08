package club.nsdn.nyasamatelecom;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NyaSama Telecom entry point (Minecraft 1.21.1 / NeoForge).
 *
 * <p>The 1.12.2 shape used {@code @Mod.Instance}, {@code @SidedProxy} and
 * {@code @Mod.EventHandler} preInit/init/postInit; as in {@code nyasamaoptics} the proxies are gone and
 * everything that used to happen in {@code CommonProxy#preInit} -- the block loader, the item loader, the
 * tile entity loader, the creative tab and the network wrapper -- happens on the mod bus from this
 * constructor instead.</p>
 *
 * <h2>Wiring (this is the part that must not be taken on trust)</h2>
 * <p>The four calls below are the module's registration layer, and they are called <b>here and nowhere
 * else</b>. This project has already paid once for a registration layer that compiled but was never
 * invoked ({@code docs/13-WAVE-BRIEFS.md} §15: optics' 33+ blocks were "compiling" for many builds without
 * ever being registered), so the calls are deliberately direct statements in the constructor rather than a
 * helper indirection, and the module report ships the {@code javap -c} excerpt that shows them in this
 * class's {@code <init>}.</p>
 * <ul>
 *   <li>{@link TelecomBlocks#register(IEventBus)} -- 20 blocks + their 20 block items;</li>
 *   <li>{@link TelecomItems#register(IEventBus)} -- the 4 real items;</li>
 *   <li>{@link TelecomBlockEntities#register(IEventBus)} -- 14 block entity types + the
 *       {@code RegisterTicketControllersEvent} listener for the chunk-loading anchor;</li>
 *   <li>{@link TelecomCreativeTab#register(IEventBus)} -- the {@code tabNyaSamaTelecom} tab.</li>
 * </ul>
 *
 * <p>Not listed above, on purpose: {@code event.*} handlers register themselves through
 * {@code @EventBusSubscriber}, {@code TelecomNetwork} through the same annotation on the mod bus, and the
 * block entity renderers through {@code TelecomRenderers} (again {@code @EventBusSubscriber}, client side).
 * {@code event.EventRegister} documents that mapping and is not called from here -- see its javadoc for why
 * a second call path was rejected.</p>
 *
 * <p>{@link TelecomCommands} registers {@code /ngt} on the game bus from its own annotation; that class and
 * its semantics are unchanged.</p>
 */
@Mod(NyaSamaTelecom.MODID)
public class NyaSamaTelecom {

    public static final String MODID = "nyasamatelecom";
    public static final Logger LOGGER = LoggerFactory.getLogger("NyaSamaTelecom");

    public NyaSamaTelecom(IEventBus modBus, ModContainer container) {
        TelecomBlocks.register(modBus);
        TelecomItems.register(modBus);
        TelecomBlockEntities.register(modBus);
        TelecomCreativeTab.register(modBus);
        LOGGER.info("[{}] v3 loaded (NeoForge, MC 1.21.1)", MODID);
    }
}
