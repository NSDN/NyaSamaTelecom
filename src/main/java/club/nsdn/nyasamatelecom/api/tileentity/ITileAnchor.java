package club.nsdn.nyasamatelecom.api.tileentity;

import net.neoforged.neoforge.common.world.chunk.TicketController;

import javax.annotation.Nullable;

/**
 * Ported from the 1.12.2 {@code ITileAnchor} (added 2020-04-07): a device that can hold a chunk-loading
 * ticket.
 *
 * <p>The parameter type is the one real change in this file. 1.12.2 used Forge's
 * {@code ForgeChunkManager.Ticket}, which no longer exists; verified against
 * neoforge-21.1.256-merged.jar, the modern mechanism is the ticket API in
 * {@code net.neoforged.neoforge.common.world.chunk}: a chunk is forced through a mod-scoped
 * {@link TicketController} keyed by a {@code ResourceLocation} (registered through
 * {@code RegisterTicketControllersEvent}), and a controller is what a loader hands back to a block
 * entity, so it is the direct analogue of the old Ticket. {@code forceChunkLoading()} keeps its 1.12.2
 * no-argument shape.</p>
 *
 * <p><b>Why not {@code SimpleTicket}</b> -- the type this parameter carried before this change: that
 * class is a manager-backed validity ticket, not a chunk mechanism.
 * {@code neoforged.neoforge.common.world.chunk} contains <em>no</em> {@code SimpleTicket} subtype at all
 * (javap on the merged jar: {@code TicketController}, {@code ForcedChunkManager}, {@code TicketHelper},
 * {@code TicketSet}, {@code LoadingValidationCallback} only), and the jar's single {@code SimpleTicket}
 * subclass is {@code common.ticket.AABBTicket}, which is a {@code SimpleTicket<Vec3>} over an
 * {@code AABB} and never touches chunks. A {@code SimpleTicket} here would therefore be a value nothing
 * could ever read -- which is exactly how the three telecom implementors carried it (their field was
 * written by {@code setChunkTicket} and nulled by {@code releaseTicket}, never read; the forcing always
 * went through {@code TelecomBlockEntities#ANCHOR}).</p>
 *
 * <p>Implementors are expected to register their {@code TicketController} (see
 * {@code RegisterTicketControllersEvent}) rather than use the old per-block-type ticket bookkeeping.</p>
 */
public interface ITileAnchor {

    void setChunkTicket(@Nullable TicketController ticket);

    void forceChunkLoading();
}
