package club.nsdn.nyasamatelecom.tileblock.wireless;

import club.nsdn.nyasamatelecom.TelecomBlockEntities;
import club.nsdn.nyasamatelecom.api.device.SignalBoxSender;
import club.nsdn.nyasamatelecom.api.tileentity.ITileAnchor;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityTransceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import cn.ac.nya.telecom.IWirelessRx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ticket.SimpleTicket;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The wireless receiver box, ported from the 1.12.2 {@code BlockWirelessRx} (registry id
 * {@code signal_box_rx}).
 *
 * <p>The sender-side twin of {@link BlockWirelessTx}; the script half is identical ({@code dev}/{@code key}
 * with the same messages and the same re-registration) and the chunk-anchor half is a line-for-line copy,
 * as it was in 1.12.2 -- the two classes really were duplicates there, and merging them here would change
 * the class shapes the rest of the port (and the renderer bindings) refer to. See
 * {@link BlockWirelessTx}'s javadoc for the {@code ForgeChunkManager} -&gt; {@code TicketController} port
 * notes, which apply unchanged.</p>
 *
 * <p>The one behavioural difference between the twins is direction: the receiver's
 * {@code setState(boolean)} writes {@code isEnabled}, and the transmitter's {@code getState()} reads it.
 * That is what lets {@link TelecomProcessor} drive this box from a script running on another device.</p>
 */
public class BlockWirelessRx extends SignalBoxSender {

    public static class TileEntityWirelessRx extends SignalBoxSender.TileEntitySignalBoxSender
            implements IWirelessRx<TileEntityTransceiver, TileEntityWirelessRx>, ITileAnchor {

        public String id = "null";
        public String key = "null";

        public TileEntityWirelessRx(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public TileEntityWirelessRx me() {
            return this;
        }

        @Override
        public void setState(boolean state) {
            this.isEnabled = state;
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            this.id = tagCompound.getString("deviceID");
            this.key = tagCompound.getString("deviceKey");
            super.fromNBT(tagCompound);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putString("deviceID", id);
            tagCompound.putString("deviceKey", key);
            return super.toNBT(tagCompound);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            super.updateSignal(level, pos);

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            if (blockEntity instanceof TileEntityWirelessRx dev) {
                if (TelecomProcessor.instance().device(dev.id) == null)
                    TelecomProcessor.instance().register(dev);
            }
        }

        /* -------- -------- -------- -------- */

        public boolean hasTicket = false;
        public static final byte ANCHOR_RADIUS = 2;
        /** 1.12.2 {@code setChunkListDepth(MAX_CHUNKS)}; no counterpart on {@code TicketController}. */
        public static final byte MAX_CHUNKS = 25;
        public Set<ChunkPos> chunks;
        /** The single {@code ITileAnchor} member 1.12.2 filled; see the class note. */
        public SimpleTicket<?> ticket;
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
        public void setChunkTicket(@Nullable SimpleTicket<?> tick) {
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

    public BlockWirelessRx(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityWirelessRx(type, pos, state);
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
        if (blockEntity instanceof TileEntityWirelessRx wireless) {
            wireless.releaseTicket();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** 1.12.2 {@code onBlockAdded}. */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
                           boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityWirelessRx wireless) {
            TelecomProcessor.instance().register(wireless);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityWirelessRx box && !stack.isEmpty()) {
            ListTag list = Util.getTagListFromNGT(stack);
            if (list == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (!level.isClientSide) {
                String[][] code = NSASM.getCode(list);

                new NSASM(code) {
                    @Override
                    public Level getWorld() {
                        return level;
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
                    public Player getPlayer() {
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

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
