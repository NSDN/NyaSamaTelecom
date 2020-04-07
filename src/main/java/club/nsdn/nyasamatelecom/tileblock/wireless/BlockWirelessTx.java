package club.nsdn.nyasamatelecom.tileblock.wireless;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBoxGetter;
import club.nsdn.nyasamatelecom.api.tileentity.ITileAnchor;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityTransceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.network.NetworkWrapper;
import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.thewdj.telecom.IWirelessTx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class BlockWirelessTx extends SignalBoxGetter {

    public static class TileEntityWirelessTx extends SignalBoxGetter.TileEntitySignalBoxGetter
        implements IWirelessTx<TileEntityTransceiver, TileEntityWirelessTx>, ITileAnchor {

        public String id = "null";
        public String key = "null";

        @Override
        public String id() {
            return id;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public TileEntityWirelessTx me() {
            return this;
        }

        @Override
        public boolean getState() {
            return isEnabled;
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            this.id = tagCompound.getString("deviceID");
            this.key = tagCompound.getString("deviceKey");
            super.fromNBT(tagCompound);
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
            tagCompound.setString("deviceID", id);
            tagCompound.setString("deviceKey", key);
            return super.toNBT(tagCompound);
        }

        @Override
        public void updateSignal(World world, BlockPos pos) {
            super.updateSignal(world, pos);

            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity == null) return;
            if (tileEntity instanceof TileEntityWirelessTx) {
                TileEntityWirelessTx dev = (TileEntityWirelessTx) tileEntity;
                if (TelecomProcessor.instance().device(dev.id) == null)
                    TelecomProcessor.instance().register(dev);
            }
        }

        /* -------- -------- -------- -------- */

        public boolean hasTicket = false;
        public static final byte ANCHOR_RADIUS = 2;
        public static final byte MAX_CHUNKS = 25;
        public Set<ChunkPos> chunks;
        public ForgeChunkManager.Ticket ticket;

        public void setTicketFlag(boolean flag) {
            hasTicket = flag;
        }

        public boolean hasTicketFlag() {
            return hasTicket;
        }

        @Override
        public void update() {
            super.update();

            if (world.isRemote) {
                if (hasTicketFlag())
                    if (chunks == null)
                        setupChunks();
                return;
            }

            if (ticket == null)
                requestTicket();
        }

        public void releaseTicket() {
            ForgeChunkManager.releaseTicket(ticket);
            ticket = null;
            setTicketFlag(false);
        }

        private boolean requestTicket() {
            ForgeChunkManager.Ticket chunkTicket = ForgeChunkManager.requestTicket(
                    NyaSamaTelecom.getInstance(), world, ForgeChunkManager.Type.NORMAL
            );
            if (chunkTicket != null) {
                NBTTagCompound tag = chunkTicket.getModData();
                tag.setInteger("x", pos.getX());
                tag.setInteger("y", pos.getY());
                tag.setInteger("z", pos.getZ());
                chunkTicket.setChunkListDepth(MAX_CHUNKS);
                setChunkTicket(chunkTicket);
                forceChunkLoading();
                return true;
            }
            return false;
        }

        public void setChunkTicket(@Nullable ForgeChunkManager.Ticket tick) {
            if (this.ticket != tick)
                ForgeChunkManager.releaseTicket(this.ticket);
            this.ticket = tick;
            setTicketFlag(ticket != null);
        }

        public Set<ChunkPos> getChunksAround(int xChunk, int zChunk, int radius) {
            Set<ChunkPos> chunkList = new HashSet<ChunkPos>();
            for (int xx = xChunk - radius; xx <= xChunk + radius; xx++) {
                for (int zz = zChunk - radius; zz <= zChunk + radius; zz++) {
                    chunkList.add(new ChunkPos(xx, zz));
                }
            }
            return chunkList;
        }

        public void forceChunkLoading() {
            if (ticket == null)
                return;

            int xChunk = getPos().getX() >> 4, zChunk = getPos().getZ() >> 4;
            setupChunks();

            Set<ChunkPos> innerChunks = getChunksAround(xChunk, zChunk, 1);

            for (ChunkPos chunk : chunks) {
                ForgeChunkManager.forceChunk(ticket, chunk);
                ForgeChunkManager.reorderChunk(ticket, chunk);
            }
            for (ChunkPos chunk : innerChunks) {
                ForgeChunkManager.forceChunk(ticket, chunk);
                ForgeChunkManager.reorderChunk(ticket, chunk);
            }

            ChunkPos myChunk = new ChunkPos(xChunk, zChunk);
            ForgeChunkManager.forceChunk(ticket, myChunk);
            ForgeChunkManager.reorderChunk(ticket, myChunk);
        }

        public void setupChunks() {
            int xChunk = getPos().getX() >> 4, zChunk = getPos().getZ() >> 4;
            if (hasTicketFlag())
                chunks = getChunksAround(xChunk, zChunk, ANCHOR_RADIUS);
            else
                chunks = Collections.emptySet();
        }

    }

    @Override
    public TileEntity createNewTileEntity() {
        return new TileEntityWirelessTx();
    }

    public BlockWirelessTx() {
        super(NyaSamaTelecom.MODID, "BlockWirelessTx", "signal_box_tx");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        list.add(TextFormatting.LIGHT_PURPLE + "Anchor (R) inside");
        list.add(TextFormatting.DARK_PURPLE + "from NSDN (C) 2020");
    }

    @Override
    public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityWirelessTx) {
            ((TileEntityWirelessTx) tileEntity).releaseTicket();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityWirelessTx) {
            TelecomProcessor.instance().register((TileEntityWirelessTx) tileEntity);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)  {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityWirelessTx) {
            TileEntityWirelessTx box = (TileEntityWirelessTx) tileEntity;

            ItemStack stack = player.getHeldItem(hand);
            if (!stack.isEmpty()) {
                NBTTagList list = Util.getTagListFromNGT(stack);
                if (list == null) return false;

                if (!world.isRemote) {
                    String[][] code = NSASM.getCode(list);

                    new NSASM(code) {
                        @Override
                        public World getWorld() {
                            return world;
                        }

                        @Override
                        public SimpleNetworkWrapper getWrapper() {
                            return NetworkWrapper.instance;
                        }

                        @Override
                        public double getX() {
                            return pos.getX();
                        }

                        @Override
                        public double getY() {
                            return pos.getY();
                        }

                        @Override
                        public double getZ() {
                            return pos.getZ();
                        }

                        @Override
                        public EntityPlayer getPlayer() {
                            return player;
                        }

                        @Override
                        public void loadFunc(LinkedHashMap<String, Operator> funcList) {
                            funcList.put("dev", (dst, src) -> {
                                if (src != null) return Result.ERR;

                                if (dst == null) {
                                    Util.say(getPlayer(), "info.signal.box.wireless.id", box.id);
                                } else {
                                    if (dst.type != RegType.STR) return Result.ERR;
                                    box.id = dst.data.toString();
                                    Util.say(getPlayer(), "info.signal.box.wireless.set");
                                }

                                return Result.OK;
                            });

                            funcList.put("key", (dst, src) -> {
                                if (src != null) return Result.ERR;

                                if (dst == null) {
                                    Util.say(getPlayer(), "info.signal.box.wireless.key", box.key);
                                } else {
                                    if (dst.type != RegType.STR) return Result.ERR;
                                    box.key = dst.data.toString();
                                    Util.say(getPlayer(), "info.signal.box.wireless.set");
                                }

                                return Result.OK;
                            });
                        }
                    }.run();

                    TelecomProcessor.instance().register(box);
                }

                return true;
            }
        }

        return false;
    }

}
