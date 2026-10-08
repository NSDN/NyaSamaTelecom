package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.TelecomBlockEntities;
import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.api.tileentity.ITileAnchor;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.api.util.Util;
import club.nsdn.nyasamatelecom.item.NyaGameMR;
import club.nsdn.nyasamatelecom.network.NSPGAPacket;
import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import cn.ac.nya.nspga.flex.BinUtil;
import cn.ac.nya.nspga.flex.INSPGAFlex;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The complex programmable logic device (the Rhino/JS-backed NSPGA variant), ported from the 1.12.2
 * {@code BlockNSPGAFlex}. Three instances exist ({@code nspga_f011nhv1}, {@code nspga_f211nhv1} with
 * {@code ioCount = 8}, {@code nspga_f344nhv1} with {@code ioCount = 32}).
 *
 * <p>The timing and failure semantics are carried over exactly, because they are the block's character:</p>
 * <ul>
 *   <li>the device is only driven while its output is <b>off</b> ({@code !dev.isEnabled}) and only once per
 *       {@code timeBase} ticks -- the counter is reset after each run, not decremented;</li>
 *   <li>{@link INSPGAFlex#schedule} runs the script off-thread with a time budget, and the
 *       <b>timeout callback</b> is a real feature, not an error path: it releases the ticket, deletes the
 *       block and sets off an explosion plus a visual-only lightning bolt;</li>
 *   <li>{@code tryInitialize} re-creates the device from the stored code every time it is called, and is
 *       retried from {@code updateSignal} on every tick while {@code device == null || !configured}.</li>
 * </ul>
 *
 * <h2>1.12.2 APIs that no longer exist</h2>
 * <ul>
 *   <li>{@code ((WorldServer) world).addScheduledTask(r)} -&gt;
 *       {@code ((ServerLevel) level).getServer().execute(r)}. Both mean "run this on the server thread";
 *       {@code MinecraftServer#execute} is the surviving entry point.</li>
 *   <li>{@code world.removeTileEntity(pos)} + {@code world.setBlockToAir(pos)} collapse into
 *       {@code level.removeBlock(pos, false)} -- in 1.21.1 removing the block removes its block entity.</li>
 *   <li>{@code world.createExplosion(null, x, y, z, 2, false)} -&gt;
 *       {@code level.explode(null, x, y, z, 2.0F, Level.ExplosionInteraction.BLOCK)}. The old call destroyed
 *       blocks and did not set fire, which is exactly {@code BLOCK} (as opposed to {@code NONE}, which
 *       would destroy nothing, or {@code TNT}, which would set fire).</li>
 *   <li>{@code new EntityLightningBolt(world, x, y, z, true)} -&gt; an {@code EntityType.LIGHTNING_BOLT}
 *       created through its type and {@code moveTo}, with {@code setVisualOnly(true)} for the old
 *       {@code effectOnly = true} flag.</li>
 *   <li>{@code player.getHeldItem(hand)} is the {@code useItemOn} stack parameter,
 *       {@code player.inventory.mainInventory.get(i)} is {@code player.getInventory().getItem(i)}, and
 *       {@code NetworkWrapper.instance.sendTo(packet, mp)} is
 *       {@code PacketDistributor.sendToPlayer(mp, packet)}.</li>
 *   <li>{@code ForgeChunkManager} -&gt; {@code TicketController}; see {@link BlockWirelessTx}'s javadoc,
 *       which states the whole mapping. The one difference here is that the 1.12.2 ticket was also
 *       released from the timeout callback, and that call is preserved.</li>
 * </ul>
 *
 * <p>{@code texturePrint} lost its {@code @SideOnly(CLIENT)} for the same reason as in
 * {@link BlockNSPGA}.</p>
 */
public class BlockNSPGAFlex extends SignalBox {

    public final Class<? extends INSPGAFlex> dev;

    /** 1.12.2 {@code @SideOnly(Side.CLIENT) ResourceLocation texturePrint}; see the class note. */
    public ResourceLocation texturePrint;

    public final String name;
    public final int ioCount;

    public static class TileEntityNSPGAFlex extends SignalBox.TileEntitySignalBox implements ITileAnchor {

        public INSPGAFlex device = null;
        public boolean configured = false;
        public int timeBase = 1;
        private int counter = 0;
        private long output = 0;

        public ArrayList<String> inputs = new ArrayList<>();
        public ArrayList<String> outputs = new ArrayList<>();
        public String code = "";

        public TileEntityNSPGAFlex(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            setInfo(8, 0.875, 0.125, 0.875);
        }

        public static void loadFromTag(TileEntityNSPGAFlex dev, CompoundTag tagCompound) {
            dev.inputs.clear(); dev.outputs.clear();
            ListTag list = tagCompound.getList("inputs", 8);
            if (!list.isEmpty())
                for (int i = 0; i < list.size(); i++)
                    dev.inputs.add(list.getString(i));
            list = tagCompound.getList("outputs", 8);
            if (!list.isEmpty())
                for (int i = 0; i < list.size(); i++)
                    dev.outputs.add(list.getString(i));
            dev.code = tagCompound.getString("code");
        }

        public static void tryInitialize(TileEntityNSPGAFlex dev) {
            Level world = dev.getLevel();
            if (world == null) return;
            if (dev.inputs.isEmpty() && dev.outputs.isEmpty())
                return;
            if (dev.code == null || dev.code.isEmpty())
                return;

            // 1.12.2 read the block back off the world (world.getBlockState(dev.getPos()).getBlock()) so that
            // the three variants each instantiate their own INSPGAFlex; kept identical here.
            if (world.getBlockState(dev.getBlockPos()).getBlock() instanceof BlockNSPGAFlex block) {
                dev.configured = false;
                dev.device = block.createDevice(dev.code);
                INSPGAFlex.schedule(() -> {
                    dev.device.initialize();
                    int tb = dev.device.timebase();
                    if (world instanceof ServerLevel serverLevel)
                        serverLevel.getServer().execute(() -> {
                            dev.configured = true;
                            dev.timeBase = tb;
                        });
                }, () -> {
                    if (world instanceof ServerLevel serverLevel)
                        serverLevel.getServer().execute(() -> explodeAndRemove(dev, serverLevel));
                });
            }
        }

        /** 1.12.2's timeout body, shared by {@code tryInitialize} and the tick loop. */
        private static void explodeAndRemove(TileEntityNSPGAFlex dev, ServerLevel serverLevel) {
            BlockPos pos = dev.getBlockPos();
            dev.releaseTicket();
            serverLevel.removeBlock(pos, false);
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            serverLevel.explode(null, x, y, z, 2.0F, Level.ExplosionInteraction.BLOCK);
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(x, y, z);
                bolt.setVisualOnly(true);
                serverLevel.addFreshEntity(bolt);
            }
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            super.fromNBT(tagCompound);

            loadFromTag(this, tagCompound);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            if (!inputs.isEmpty()) {
                ListTag list = new ListTag();
                for (String s : inputs)
                    list.add(StringTag.valueOf(s));
                tagCompound.put("inputs", list);
            }
            if (!outputs.isEmpty()) {
                ListTag list = new ListTag();
                for (String s : outputs)
                    list.add(StringTag.valueOf(s));
                tagCompound.put("outputs", list);
            }
            tagCompound.putString("code", code);

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
            if (info == null && dev.contains("bus")) // InnerBus
                return TelecomProcessor.instance().get(dev);
            if (!TelecomProcessor.instance().isTx(info)) {
                return false;
            }
            result = TelecomProcessor.instance().get(info);
            return result;
        }

        private void output(String dev, boolean state) {
            TelecomProcessor.DeviceInfo info;
            info = TelecomProcessor.instance().device(dev);
            if (info == null && dev.contains("bus")) { // InnerBus
                TelecomProcessor.instance().set(dev, state);
                return;
            }
            if (!TelecomProcessor.instance().isRx(info)) {
                return;
            }
            TelecomProcessor.instance().set(info, state);
        }

        public long input() {
            long result = 0x00;
            for (int i = 0; i < inputs.size(); i++)
                result |= ((input(inputs.get(i)) ? 0x1 : 0x0) << i);
            return BinUtil.uint32_t(result);
        }

        public void output(long data) {
            data = BinUtil.uint32_t(data);
            for (int i = 0; i < outputs.size(); i++)
                output(outputs.get(i), ((data >> i) & 0x1) != 0);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            if (blockEntity instanceof TileEntityNSPGAFlex dev) {

                int meta = dev.META;
                int old = meta;
                boolean isEnabled;

                if (dev.getSender() == null) {
                    isEnabled = (meta & 0x8) != 0;
                    meta &= 0x7;
                } else {
                    isEnabled = dev.senderIsPowered();

                    if (isEnabled) meta |= 0x8;
                    else meta &= 0x7;
                }

                dev.isEnabled = isEnabled;

                if (dev.inverterEnabled)
                    dev.isEnabled = !dev.isEnabled;

                if (dev.device == null || !dev.configured)
                    tryInitialize(dev);

                boolean state = false;
                if (dev.device != null && dev.configured && !dev.isEnabled) {
                    if (dev.counter < dev.timeBase)
                        dev.counter += 1;
                    if (dev.counter >= dev.timeBase) {
                        dev.counter = 0;

                        long input = dev.input();
                        dev.output(dev.output);

                        INSPGAFlex.schedule(() -> {
                            long output = dev.device.output(input).longValue();
                            if (level instanceof ServerLevel serverLevel)
                                serverLevel.getServer().execute(() -> {
                                    dev.output = output;
                                });
                        }, () -> {
                            if (level instanceof ServerLevel serverLevel)
                                serverLevel.getServer().execute(() -> explodeAndRemove(dev, serverLevel));
                        });
                    }
                    state = true;
                }

                if (!tryControlFirst(state)) {
                    if (!tryControlSecond(state)) {
                        if (!setTargetSender(state)) {
                            if (!setTargetGetter(state)) {
                                if (getTarget() != null) {
                                    controlTarget(state);
                                }
                            }
                        }
                    }
                }

                if (old != meta) {
                    dev.META = meta;
                    dev.refresh();
                }
            }
        }

        /* -------- -------- -------- -------- */

        public boolean hasTicket = false;
        public static final byte ANCHOR_RADIUS = 2;
        /** 1.12.2 {@code setChunkListDepth(MAX_CHUNKS)}; no counterpart on {@code TicketController}. */
        public static final byte MAX_CHUNKS = 25;
        public Set<ChunkPos> chunks;
        /** The single {@code ITileAnchor} member 1.12.2 filled; see {@link BlockWirelessTx}'s javadoc. */
        public TicketController ticket;
        private final Set<ChunkPos> forcedChunks = new HashSet<>();

        public void setTicketFlag(boolean flag) {
            hasTicket = flag;
        }

        public boolean hasTicketFlag() {
            return hasTicket;
        }

        @Override
        public void tick() {
            super.tick();

            if (level == null) return;

            if (level.isClientSide) {
                if (hasTicketFlag())
                    if (chunks == null)
                        setupChunks();
                return;
            }

            if (!hasTicket)
                requestTicket();
        }

        public void releaseTicket() {
            if (level instanceof ServerLevel && !forcedChunks.isEmpty()) {
                for (ChunkPos chunk : new HashSet<>(forcedChunks)) {
                    force(chunk, false);
                }
            }
            forcedChunks.clear();
            ticket = null;
            setTicketFlag(false);
        }

        private boolean requestTicket() {
            if (!(level instanceof ServerLevel)) {
                return false;
            }
            setTicketFlag(true);
            forceChunkLoading();
            return true;
        }

        @Override
        public void setChunkTicket(@Nullable TicketController tick) {
            this.ticket = tick;
            setTicketFlag(tick != null);
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

        private void force(ChunkPos chunk, boolean add) {
            if (!(level instanceof ServerLevel serverLevel)) return;
            TelecomBlockEntities.ANCHOR.forceChunk(
                    serverLevel, getBlockPos(), chunk.x, chunk.z, add, true);
            if (add) forcedChunks.add(chunk);
            else forcedChunks.remove(chunk);
        }

        @Override
        public void forceChunkLoading() {
            if (!(level instanceof ServerLevel)) return;

            int xChunk = getBlockPos().getX() >> 4, zChunk = getBlockPos().getZ() >> 4;
            setupChunks();

            Set<ChunkPos> innerChunks = getChunksAround(xChunk, zChunk, 1);

            for (ChunkPos chunk : chunks) {
                force(chunk, true);
            }
            for (ChunkPos chunk : innerChunks) {
                force(chunk, true);
            }

            force(new ChunkPos(xChunk, zChunk), true);
        }

        public void setupChunks() {
            int xChunk = getBlockPos().getX() >> 4, zChunk = getBlockPos().getZ() >> 4;
            if (hasTicketFlag())
                chunks = getChunksAround(xChunk, zChunk, ANCHOR_RADIUS);
            else
                chunks = Collections.emptySet();
        }
    }

    public BlockNSPGAFlex(Class<? extends INSPGAFlex> dev, String name, String id, int ioCount,
                          BlockBehaviour.Properties properties,
                          Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);

        this.dev = dev; this.name = id; this.ioCount = ioCount;
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityNSPGAFlex(type, pos, state);
    }

    public INSPGAFlex createDevice(String code) {
        try {
            return dev.getConstructor(String.class).newInstance(code);
        } catch (Exception e) {
            return null;
        }
    }

    /** 1.12.2 {@code addInformation}: the same two coloured lines, colours preserved. */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.literal("Anchor (R) inside").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("from NSDN (C) 2020").withStyle(ChatFormatting.DARK_PURPLE));
    }

    /** 1.12.2 {@code breakBlock}. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                           boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityNSPGAFlex flex) {
            flex.releaseTicket();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityNSPGAFlex dev) {

            if (!level.isClientSide && player instanceof ServerPlayer mp) {

                if (stack.getItem() instanceof NyaGameMR) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack slot = player.getInventory().getItem(i);
                        if (slot.isEmpty()) continue;
                        if (slot.getItem() == Items.AIR) continue;
                        if (slot.getItem() instanceof NGTablet) {

                            TileEntityNSPGAFlex.tryInitialize(dev);

                            Util.say(player, "info.nspga.reconf");
                            return ItemInteractionResult.SUCCESS;
                        }
                    }
                } else if (stack.getItem() instanceof NGTablet) {
                    if (dev.code == null || dev.code.isEmpty())
                        dev.code = createDevice("").getDefaultCode();

                    int in = dev.inputs.size(), out = dev.outputs.size(), io = ioCount;
                    String[] inputs = dev.inputs.toArray(new String[]{});
                    String[] outputs = dev.outputs.toArray(new String[]{});
                    if ((in == 0 && out == 0) || (in != io || out != io))
                        PacketDistributor.sendToPlayer(mp, new NSPGAPacket(pos, io, dev.code));
                    else
                        PacketDistributor.sendToPlayer(mp, new NSPGAPacket(pos, io, inputs, outputs, dev.code));

                    return ItemInteractionResult.SUCCESS;
                }
            } else if (level.isClientSide) {
                // 1.12.2 returned true here so the client would not fall through to the block's default use.
                Item item = stack.getItem();
                return item instanceof NyaGameMR || item instanceof NGTablet
                        ? ItemInteractionResult.SUCCESS
                        : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
