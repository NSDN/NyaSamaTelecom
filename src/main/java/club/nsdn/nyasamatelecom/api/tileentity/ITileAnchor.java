package club.nsdn.nyasamatelecom.api.tileentity;

import net.neoforged.neoforge.common.ticket.SimpleTicket;

import javax.annotation.Nullable;

/**
 * Ported from the 1.12.2 {@code ITileAnchor} (added 2020-04-07): a device that can hold a chunk-loading
 * ticket.
 *
 * <p>The parameter type is the one real change in this file. 1.12.2 used Forge's
 * {@code ForgeChunkManager.Ticket}, which no longer exists; verified against
 * neoforge-21.1.256-merged.jar, the modern mechanism is the ticket API in
 * {@code net.neoforged.neoforge.common.ticket} ({@code SimpleTicket}, {@code ChunkTicketManager},
 * {@code ITicketManager}) plus {@code common.world.chunk.TicketController} for registering a
 * controller. {@link SimpleTicket} is used here as the direct analogue of the old Ticket.</p>
 *
 * <p>Implementors that also need to raise the ticket are expected to register a
 * {@code TicketController} (see {@code RegisterTicketControllersEvent}) rather than the old
 * per-block-type ticket bookkeeping.</p>
 */
public interface ITileAnchor {

    void setChunkTicket(@Nullable SimpleTicket<?> ticket);

    void forceChunkLoading();
}
