package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.SignalBoxSender;
import club.nsdn.nyasamatelecom.api.tileentity.ITriStateReceiver;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The timing counter, ported from the 1.12.2 {@code BlockTimer} (registry id {@code timer}).
 *
 * <p>The counting rule is the 1.12.2 one and is deliberately not "improved": a POS latch step increments
 * {@code tmpTime} up to {@code setTime}, a NEG step clears it, anything below {@code setTime} disables the
 * output, and {@code autoReload} decides whether reaching the target restarts the count. The transceiver
 * gate ({@code isEnabled && transceiverIsPowered()}) is applied <b>after</b> that decision, exactly as
 * before.</p>
 *
 * <p>The script interface keeps its three functions: {@code set} (clamped at 0 for negatives),
 * {@code auto} and the overridden {@code rst}. As in 1.12.2, {@code set}/{@code auto} require an INT
 * destination register and reject a source operand.</p>
 */
public class BlockTimer extends SignalBoxSender {

    public static class TileEntityTimer extends SignalBoxSender.TileEntitySignalBoxSender
            implements ITriStateReceiver {

        public int setTime = 20;
        public int tmpTime;
        public boolean autoReload = true;

        private int prevSetTime, prevTmpTime;
        private boolean prevAutoReload;

        public static final int STATE_POS = 1;
        public static final int STATE_ZERO = 0;
        public static final int STATE_NEG = -1;

        public int state;
        private int prevState;

        public TileEntityTimer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        public void setStatePos() {
            state = state == STATE_NEG ? STATE_ZERO : STATE_POS;
        }

        public void setStateNeg() {
            state = state == STATE_POS ? STATE_ZERO : STATE_NEG;
        }

        public boolean hasChanged() {
            return (prevIsEnabled != isEnabled) || (prevSetTime != setTime) ||
                    (prevTmpTime != tmpTime) || (prevAutoReload != autoReload) ||
                    (prevState != state);
        }

        public void updateChanged() {
            prevIsEnabled = isEnabled;
            prevSetTime = setTime;
            prevTmpTime = tmpTime;
            prevAutoReload = autoReload;
            prevState = state;
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            setTime = tagCompound.getInt("setTime");
            tmpTime = tagCompound.getInt("tmpTime");
            autoReload = tagCompound.getBoolean("autoReload");
            state = tagCompound.getInt("state");
            super.fromNBT(tagCompound);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putInt("setTime", setTime);
            tagCompound.putInt("tmpTime", tmpTime);
            tagCompound.putBoolean("autoReload", autoReload);
            tagCompound.putInt("state", state);
            return super.toNBT(tagCompound);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            if (blockEntity instanceof TileEntityTimer timer) {

                int meta = timer.META;
                int old = meta;

                switch (timer.state) {
                    case TileEntityTimer.STATE_POS:
                        if (timer.tmpTime < timer.setTime)
                            timer.tmpTime += 1;
                        break;
                    case TileEntityTimer.STATE_NEG:
                        timer.tmpTime = 0;
                        break;
                    case TileEntityTimer.STATE_ZERO:
                        break;
                    default:
                        break;
                }

                if (timer.tmpTime >= timer.setTime) {
                    timer.isEnabled = true;
                    if (timer.autoReload) timer.tmpTime = 0;
                } else {
                    timer.isEnabled = false;
                }

                boolean isEnabled = timer.isEnabled;

                if (timer.getTransceiver() != null) {
                    isEnabled = isEnabled && timer.transceiverIsPowered();
                }

                if (isEnabled) meta |= 0x8;
                else meta &= 0x7;

                if (old != meta) {
                    timer.META = meta;
                }
                if (timer.hasChanged()) {
                    timer.updateChanged();
                    timer.refresh();
                }

                timer.state = TileEntityTimer.STATE_ZERO;
            }
        }
    }

    public abstract static class TimerConfigurer extends NSASM {

        public TimerConfigurer(String[][] code) { super(code); }

        public TimerConfigurer(String code) { super(code); }

        public abstract TileEntityTimer getTimer();

        private void prt(String str) {
            Register result = new Register();
            result.type = RegType.STR; result.strPtr = 0;
            result.readOnly = true;
            result.data = str;
            funcList.get("prt").run(result, null);
        }

        @Override
        public void loadFunc(LinkedHashMap<String, Operator> funcList) {
            funcList.put("set", (dst, src) -> {
                if (src != null) return Result.ERR;
                if (dst == null) return Result.ERR;
                if (dst.type != RegType.INT) return Result.ERR;

                int t = (int) dst.data;
                if (getTimer() != null) {
                    getTimer().setTime = t < 0 ? 0 : t;
                }
                return Result.OK;
            });

            funcList.put("auto", (dst, src) -> {
                if (src != null) return Result.ERR;
                if (dst == null) return Result.ERR;
                if (dst.type != RegType.INT) return Result.ERR;

                int auto = (int) dst.data;
                if (getTimer() != null) {
                    getTimer().autoReload = auto > 0;
                }
                return Result.OK;
            });

            funcList.replace("rst", (dst, src) -> {
                if (dst != null || src != null)
                    return Result.ERR;
                if (getTimer() != null) {
                    getTimer().tmpTime = 0;
                }
                return Result.OK;
            });
        }
    }

    public BlockTimer(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityTimer(type, pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityTimer timer && !stack.isEmpty()) {
            ListTag list = Util.getTagListFromNGT(stack);
            if (list == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (!level.isClientSide) {
                String[][] code = NSASM.getCode(list);
                new TimerConfigurer(code) {
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
                    public TileEntityTimer getTimer() {
                        return timer;
                    }
                }.run();
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
