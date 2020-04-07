package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.api.tileentity.ITileAnchor;
import club.nsdn.nyasamatelecom.api.tool.ToolBase;
import club.nsdn.nyasamatelecom.api.util.Util;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.network.NSPGAPacket;
import club.nsdn.nyasamatelecom.network.NetworkWrapper;
import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import cn.ac.nya.nspga.flex.INSPGAFlex;
import cn.ac.nya.nspga.flex.NSPGAEditor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by drzzm32 on 2020.4.7.
 */
public class BlockNSPGAFlex extends SignalBox {

    public final Class<? extends INSPGAFlex> dev;

    public static class TileEntityNSPGAFlex extends TileEntitySignalBox implements ITileAnchor {

        public INSPGAFlex device = null;
        public boolean configured = false;

        public ArrayList<String> inputs = new ArrayList<>();
        public ArrayList<String> outputs = new ArrayList<>();
        public String code = "";

        public TileEntityNSPGAFlex() {
            setInfo(8, 0.875, 0.125, 0.875);
        }

        public static void loadFromTag(TileEntityNSPGAFlex dev, NBTTagCompound tagCompound) {
            dev.inputs.clear(); dev.outputs.clear();
            NBTTagList list = tagCompound.getTagList("inputs", 8);
            if (!list.hasNoTags())
                for (int i = 0; i < list.tagCount(); i++)
                    dev.inputs.add(list.getStringTagAt(i));
            list = tagCompound.getTagList("outputs", 8);
            if (!list.hasNoTags())
                for (int i = 0; i < list.tagCount(); i++)
                    dev.outputs.add(list.getStringTagAt(i));
            dev.code = tagCompound.getString("code");
        }

        public static void tryInitialize(TileEntityNSPGAFlex dev) {
            World world = dev.world;
            if (world == null) return;
            if (dev.inputs.isEmpty() && dev.outputs.isEmpty())
                return;
            if (dev.code == null || dev.code.isEmpty())
                return;

            Block block = world.getBlockState(dev.getPos()).getBlock();
            if (block instanceof BlockNSPGAFlex) {
                dev.configured = false;
                dev.device = ((BlockNSPGAFlex) block).createDevice(dev.code);
                INSPGAFlex.schedule(() -> {
                    dev.device.initialize();
                    if (world instanceof WorldServer)
                        ((WorldServer) world).addScheduledTask(() -> dev.configured = true);
                });
            }
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            super.fromNBT(tagCompound);

            loadFromTag(this, tagCompound);
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
            if (!inputs.isEmpty()) {
                NBTTagList list = new NBTTagList();
                for (String s : inputs)
                    list.appendTag(new NBTTagString(s));
                tagCompound.setTag("inputs", list);
            }
            if (!outputs.isEmpty()) {
                NBTTagList list = new NBTTagList();
                for (String s : outputs)
                    list.appendTag(new NBTTagString(s));
                tagCompound.setTag("outputs", list);
            }
            tagCompound.setString("code", code);

            return super.toNBT(tagCompound);
        }

        @Override
        public boolean tryControlFirst(boolean state) {
            return false;
        }

        @Override
        public boolean tryControlSecond(boolean state) {
            return false;
        }

        private boolean input(String dev) {
            TelecomProcessor.DeviceInfo info;
            info = TelecomProcessor.instance().device(dev);
            boolean result;
            if (info == null) return false;
            if (!TelecomProcessor.instance().isTx(info)) {
                return false;
            }
            result = TelecomProcessor.instance().get(info);
            return result;
        }

        private void output(String dev, boolean state) {
            TelecomProcessor.DeviceInfo info;
            info = TelecomProcessor.instance().device(dev);
            if (info == null) return;
            if (!TelecomProcessor.instance().isRx(info)) {
                return;
            }
            TelecomProcessor.instance().set(info, state);
        }

        public int input() {
            int result = 0x00;
            for (int i = 0; i < inputs.size(); i++)
                result |= ((input(inputs.get(i)) ? 0x1 : 0x0) << i);
            return result;
        }

        public void output(int data) {
            for (int i = 0; i < outputs.size(); i++)
                output(outputs.get(i), ((data >> i) & 0x1) != 0);
        }

        @Override
        public void updateSignal(World world, BlockPos pos) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity == null) return;
            if (tileEntity instanceof TileEntityNSPGAFlex) {
                TileEntityNSPGAFlex dev = (TileEntityNSPGAFlex) tileEntity;

                if (dev.device == null || !dev.configured)
                    tryInitialize(this);

                if (dev.device != null && dev.configured) {
                    int input = dev.input();
                    int output = dev.device.output(input).intValue();
                    dev.output(output);
                    dev.refresh();
                }
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

    @SideOnly(Side.CLIENT)
    public ResourceLocation texturePrint;

    public final String name;
    public final int ioCount;

    @Override
    public TileEntity createNewTileEntity() {
        return new TileEntityNSPGAFlex();
    }

    public BlockNSPGAFlex(Class<? extends INSPGAFlex> dev, String name, String id, int ioCount) {
        super(NyaSamaTelecom.MODID, name, id);
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);

        this.dev = dev; this.name = id; this.ioCount = ioCount;
    }

    public INSPGAFlex createDevice(String code) {
        try {
            return dev.getConstructor(String.class).newInstance(code);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        list.add(TextFormatting.LIGHT_PURPLE + "Anchor (R) inside");
        list.add(TextFormatting.DARK_PURPLE + "from NSDN (C) 2020");
    }

    @Override
    public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityNSPGAFlex) {
            ((TileEntityNSPGAFlex) tileEntity).releaseTicket();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)  {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityNSPGAFlex) {
            TileEntityNSPGAFlex dev = (TileEntityNSPGAFlex) tileEntity;

            ItemStack stack;
            if (!world.isRemote && player.isSneaking()) {
                for (int i = 0; i < 9; i++) {
                    stack = player.inventory.mainInventory.get(i);
                    if (stack.isEmpty()) continue;
                    if (stack.getItem() == Items.AIR) continue;
                    if (stack.getItem() instanceof ToolBase) {

                        TileEntityNSPGAFlex.tryInitialize(dev);

                        Util.say(player, "info.nspga.reconf");
                        return true;
                    }
                }
            }

            stack = player.getHeldItem(hand);
            if (world.isRemote && stack.getItem() instanceof ToolBase) {
                if (dev.code == null || dev.code.isEmpty())
                    dev.code = createDevice("").getDefaultCode();

                if (
                        (dev.inputs.isEmpty() && dev.outputs.isEmpty()) ||
                                (dev.inputs.size() != ioCount || dev.outputs.size() != ioCount)
                )
                    new NSPGAEditor(ioCount, ioCount, dev.code).setCallback((i, o, c) -> {
                        NetworkWrapper.instance.sendToServer(new NSPGAPacket(pos, i, o, c));
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            player.sendMessage(new TextComponentTranslation("info.nspga.flash"));
                        });
                    });
                else
                    new NSPGAEditor(dev.inputs.toArray(new String[]{}), dev.outputs.toArray(new String[]{}), dev.code)
                            .setCallback((i, o, c) -> {
                                NetworkWrapper.instance.sendToServer(new NSPGAPacket(pos, i, o, c));
                                Minecraft.getMinecraft().addScheduledTask(() -> {
                                    player.sendMessage(new TextComponentTranslation("info.nspga.flash"));
                                });
                            });

                player.sendMessage(new TextComponentTranslation("info.nspga.editor"));
                return true;
            }
        }

        return false;
    }

}
