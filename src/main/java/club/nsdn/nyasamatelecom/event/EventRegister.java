package club.nsdn.nyasamatelecom.event;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.TelecomBlockEntities;
import club.nsdn.nyasamatelecom.TelecomBlocks;
import club.nsdn.nyasamatelecom.TelecomCreativeTab;
import club.nsdn.nyasamatelecom.TelecomItems;
import net.neoforged.bus.api.IEventBus;

/**
 * The 1.12.2 {@code EventRegister} had three entry points -- {@code registerCommon()},
 * {@code registerServer()} and {@code registerClient()} -- each a list of
 * {@code MinecraftForge.EVENT_BUS.register(singleton)} calls plus one
 * {@code ForgeChunkManager.setForcedChunkLoadingCallback(...)}. <b>None of them is needed any more and this
 * class does not subscribe to anything</b>; it is kept as the module's registration map, because that list
 * <i>is</i> the checklist of things that must still happen somewhere. Every line of it is accounted for:</p>
 *
 * <table border="1">
 *   <caption>1.12.2 line -&gt; where it lives now</caption>
 *   <tr><th>1.12.2</th><th>1.21.1</th></tr>
 *   <tr><td>{@code MinecraftForge.EVENT_BUS.register(BlockLoader.instance())}</td>
 *       <td>{@link TelecomBlocks}<code>#register(IEventBus)</code>, called from
 *           {@link NyaSamaTelecom}'s constructor. The class itself handled block registration, block item
 *           registration and the client icon models; only the two registrations survive, as
 *           {@code DeferredRegister}s.</td></tr>
 *   <tr><td>{@code MinecraftForge.EVENT_BUS.register(ItemLoader.instance())}</td>
 *       <td>{@link TelecomItems}<code>#register(IEventBus)</code>, same wiring.</td></tr>
 *   <tr><td>{@code MinecraftForge.EVENT_BUS.register(TileEntityLoader.instance())}</td>
 *       <td>{@link TelecomBlockEntities}<code>#register(IEventBus)</code> -- and it is the only one of the
 *           three that also takes a mod-bus listener, for
 *           {@code RegisterTicketControllersEvent}.</td></tr>
 *   <tr><td>{@code new CreativeTabLoader(event)}</td>
 *       <td>{@link TelecomCreativeTab}<code>#register(IEventBus)</code>. 1.12.2 built the tab inside the
 *           event handler; the tab is a registry entry now.</td></tr>
 *   <tr><td>{@code MinecraftForge.EVENT_BUS.register(ToolHandler.instance())}</td>
 *       <td>{@link ToolHandler} -- self-registering via {@code @EventBusSubscriber}, like every other event
 *           handler in this module.</td></tr>
 *   <tr><td>{@code MinecraftForge.EVENT_BUS.register(TelecomHandler.instance())}</td>
 *       <td>{@link TelecomHandler}, self-registering.</td></tr>
 *   <tr><td>{@code ForgeChunkManager.setForcedChunkLoadingCallback(mod, ChunkLoaderHandler.instance())}</td>
 *       <td>{@link TelecomBlockEntities}<code>#register(IEventBus)</code> adds the
 *           {@code RegisterTicketControllersEvent} listener that registers the module's
 *           {@code TicketController}. The callback itself has no successor -- see
 *           {@link ChunkLoaderHandler}.</td></tr>
 *   <tr><td>{@code registerServer()}: {@code register(ServerTickHandler.instance())}</td>
 *       <td>{@link ServerTickHandler}, self-registering. The "dedicated server only" restriction is
 *           {@code NOT PORTED}; see that class.</td></tr>
 *   <tr><td>{@code registerClient()}: {@code register(TileEntityModelBinder.instance())}</td>
 *       <td>{@code TelecomRenderers} -- block entity renderers are bound from
 *           {@code EntityRenderersEvent.RegisterRenderers}, a mod-bus event, so the class self-registers on
 *           the mod bus with {@code value = Dist.CLIENT}.</td></tr>
 *   <tr><td>{@code registerClient()}: {@code register(ClientTickHandler.instance())}</td>
 *       <td>{@link ClientTickHandler}, self-registering.</td></tr>
 * </table>
 *
 * <p>The second half of the old file -- the client/server proxies that called these three methods -- is
 * ported nowhere: {@code @SidedProxy} and the {@code FMLPreInitializationEvent} phases are gone, and
 * {@link NyaSamaTelecom}'s constructor is the single place where the mod-bus registrations happen. The
 * three proxy classes ({@code proxy.CommonProxy} / {@code ClientProxy} / {@code ServerProxy}) are therefore
 * {@code NOT PORTED} as classes; this table is their replacement, and {@code ServerProxy}'s other job (the
 * JAX-WS web service) is documented in the module report as {@code NOT PORTED} for its own reasons.</p>
 */
public final class EventRegister {

    private EventRegister() {
    }

    /**
     * Kept as the single named entry point for the mod-bus half of the old {@code registerCommon()}, and
     * kept honest: it says out loud that 1.12.2's game-bus registrations are now annotations.
     *
     * <p>It is <b>not</b> called from {@link NyaSamaTelecom}: the four registrations it would repeat are
     * already invoked there explicitly, and a second call path would make "is the registration layer really
     * wired?" harder to answer from the bytecode, which is the one property this project has been burned
     * on. It exists to be callable and self-documenting, not to be a hidden second wiring.</p>
     */
    public static void registerCommon(IEventBus modBus) {
        TelecomBlocks.register(modBus);
        TelecomItems.register(modBus);
        TelecomBlockEntities.register(modBus);
        TelecomCreativeTab.register(modBus);
    }
}
