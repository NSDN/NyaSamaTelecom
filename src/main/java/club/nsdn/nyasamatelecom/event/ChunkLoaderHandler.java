package club.nsdn.nyasamatelecom.event;

/**
 * <b>NOT PORTED.</b> The 1.12.2 {@code ChunkLoaderHandler} implemented
 * {@code ForgeChunkManager.LoadingCallback}, {@code OrderedLoadingCallback} and
 * {@code PlayerOrderedLoadingCallback} so that the chunk-loading tickets of the wireless boxes and the flex
 * NSPGA devices were re-bound to their block entities after a world load. It was installed with
 * {@code ForgeChunkManager.setForcedChunkLoadingCallback(mod, ChunkLoaderHandler.instance())} from
 * {@link EventRegister#registerCommon()}.
 *
 * <h2>Why nothing here can be ported</h2>
 * <ul>
 *   <li>{@code ForgeChunkManager.Ticket} does not exist; the modern mechanism is a
 *       {@code TicketController} keyed by a {@code ResourceLocation}, and its
 *       {@code RegisterTicketControllersEvent} registration plus the per-chunk {@code forceChunk} calls
 *       live in {@code TelecomBlockEntities} and in the two wireless block entities /
 *       {@code BlockNSPGAFlex.TileEntityNSPGAFlex} respectively. That covers
 *       {@code requestTicket}/{@code forceChunk}/{@code reorderChunk}/{@code releaseTicket}.</li>
 *   <li>{@code LoadingCallback#ticketsLoaded(tickets, world)} -- the half that re-attached a saved ticket to
 *       its block entity -- <b>has no successor at all</b>. NeoForge persists forced chunks as
 *       {@code (controller id, chunk, owner)} and {@code ForcedChunkManager.reinstatePersistentChunks}
 *       re-adds them at level load by itself, so the ownership survives without help from the mod. The
 *       same conclusion was reached for {@code nyasamarailway}'s equivalent class in an earlier wave
 *       ({@code RailwayEntities}' javadoc), and this is the second module it applies to.
 *       {@code TicketController}'s optional {@code LoadingValidationCallback} is therefore left null.</li>
 *   <li>{@code ticketsLoaded(tickets, world, maxTicketCount)} and
 *       {@code playerTicketsLoaded(tickets, world)} were ticket-budget bookkeeping
 *       ({@code OrderedLoadingCallback} / {@code PlayerOrderedLoadingCallback}). With no ticket objects to
 *       partition and no player tickets in this module, both are empty work.</li>
 * </ul>
 *
 * <p>The class is kept, empty and named, so that the 1.12.2 file list maps one-to-one onto the port: every
 * entry in it is accounted for above, and nothing is silently dropped.</p>
 */
public final class ChunkLoaderHandler {

    private ChunkLoaderHandler() {
    }
}
