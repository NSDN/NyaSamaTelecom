package club.nsdn.nyasamatelecom.event;

import club.nsdn.nyasamatelecom.api.tileentity.ITileAnchor;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by drzzm32 on 2020.4.7.
 */
public class ChunkLoaderHandler implements ForgeChunkManager.LoadingCallback, ForgeChunkManager.OrderedLoadingCallback, ForgeChunkManager.PlayerOrderedLoadingCallback {
    private static ChunkLoaderHandler instance;

    public static ChunkLoaderHandler instance() {
        if (instance == null)
            instance = new ChunkLoaderHandler();
        return instance;
    }

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        for (ForgeChunkManager.Ticket ticket : tickets) {
            if (ticket.isPlayerTicket())
                continue;
            NBTTagCompound tag = ticket.getModData();
            BlockPos pos = new BlockPos(
                    tag.getInteger("x"),
                    tag.getInteger("y"),
                    tag.getInteger("z")
            );
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ITileAnchor) {
                ITileAnchor anchor = (ITileAnchor) te;
                anchor.setChunkTicket(ticket);
                anchor.forceChunkLoading();
            }
        }
    }

    @Override
    public List<ForgeChunkManager.Ticket> ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world, int maxTicketCount) {
        Set<ForgeChunkManager.Ticket> worldTickets = new HashSet<ForgeChunkManager.Ticket>();
        Set<ForgeChunkManager.Ticket> tileTickets = new HashSet<ForgeChunkManager.Ticket>();
        for (ForgeChunkManager.Ticket ticket : tickets) {
            NBTTagCompound tag = ticket.getModData();
            BlockPos pos = new BlockPos(
                    tag.getInteger("x"),
                    tag.getInteger("y"),
                    tag.getInteger("z")
            );
            TileEntity te = world.getTileEntity(pos);
            if (te == null) {
                int y = ticket.getModData().getInteger("yCoord");
                if (y >= 0) {
                    worldTickets.add(ticket);
                }
            } else {
                if (te instanceof ITileAnchor) {
                    tileTickets.add(ticket);
                }
            }
        }

        List<ForgeChunkManager.Ticket> claimedTickets = new LinkedList<ForgeChunkManager.Ticket>();
        claimedTickets.addAll(tileTickets);
        claimedTickets.addAll(worldTickets);
        return claimedTickets;
    }

    @Override
    public ListMultimap<String, ForgeChunkManager.Ticket> playerTicketsLoaded(ListMultimap<String, ForgeChunkManager.Ticket> tickets, World world) {
        return LinkedListMultimap.create();
    }

}
