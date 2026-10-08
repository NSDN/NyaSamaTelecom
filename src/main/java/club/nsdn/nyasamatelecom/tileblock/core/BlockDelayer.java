package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import cn.ac.nya.telecom.IPassive;
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
 * The delayed signal box, ported from the 1.12.2 {@code BlockDelayer} (registry id {@code delayer}).
 *
 * <p>The delay rule is transcribed verbatim: while the input is off, {@code tmpTime} resets and the target
 * is driven with {@code inverterEnabled}; while the input is on, {@code tmpTime} counts up and the target
 * is driven with {@code !inverterEnabled} once {@code setTime} ticks have elapsed. Note that
 * {@code doControl} is a private re-implementation of the generic control chain with one difference from
 * {@code SignalBox.TileEntitySignalBox#updateSignal}: a receiver target is only driven through
 * {@code controlTarget} when it is <b>also</b> an {@link IPassive}. That asymmetry is 1.12.2's and is
 * preserved on purpose -- see the project rule on not "tidying" transcribed behaviour.</p>
 *
 * <p>{@code set} keeps its 1.12.2 shape: no destination prints the current delay, a destination must be an
 * INT register, and negatives clamp to 0. The dead local in 1.12.2's {@code tryControlSecond} spelling is
 * the same here.</p>
 */
public class BlockDelayer extends SignalBox {

    public static class TileEntityDelayer extends SignalBox.TileEntitySignalBox {

        public int setTime = 20;
        public int tmpTime;

        public TileEntityDelayer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            super.fromNBT(tagCompound);
            setTime = tagCompound.getInt("setTime");
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putInt("setTime", setTime);
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

        public void doControl(TileEntityDelayer delayer, boolean isEnabled) {
            if (!delayer.tryControlFirst(isEnabled)) {
                if (!delayer.tryControlSecond(isEnabled)) {
                    if (!delayer.setTargetSender(isEnabled)) {
                        if (!delayer.setTargetGetter(isEnabled)) {
                            if (delayer.getTarget() != null) {
                                BlockEntity target = delayer.getTarget();
                                if (target instanceof TileEntityReceiver) {
                                    if (target instanceof IPassive) {
                                        delayer.controlTarget(isEnabled);
                                    }
                                } else {
                                    delayer.controlTarget(isEnabled);
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            if (blockEntity instanceof TileEntityDelayer delayer) {

                int meta = delayer.META;
                int old = meta;
                boolean isEnabled;

                if (delayer.getSender() == null) {
                    isEnabled = (meta & 0x8) != 0;
                    meta &= 0x7;
                } else {
                    isEnabled = delayer.senderIsPowered();

                    if (isEnabled) meta |= 0x8;
                    else meta &= 0x7;
                }

                delayer.isEnabled = isEnabled;

                if (!isEnabled) {
                    delayer.tmpTime = 0;
                    doControl(delayer, delayer.inverterEnabled);
                }
                else {
                    delayer.tmpTime += 1;
                    if (delayer.tmpTime >= delayer.setTime) {
                        delayer.tmpTime = 0;
                        doControl(delayer, !delayer.inverterEnabled);
                    }
                }

                if (old != meta || delayer.prevInverterEnabled != delayer.inverterEnabled) {
                    delayer.prevInverterEnabled = delayer.inverterEnabled;
                    delayer.META = meta;
                    delayer.refresh();
                }
            }
        }
    }

    public abstract static class DelayerConfigurer extends NSASM {

        public DelayerConfigurer(String[][] code) { super(code); }

        public DelayerConfigurer(String code) { super(code); }

        public abstract TileEntityDelayer getDelayer();

        private void prt(String str) {
            Register result = new Register();
            result.type = RegType.STR; result.strPtr = 0;
            result.readOnly = true;
            result.data = str;
            funcList.get("prt").run(result, null);
        }

        @Override
        public void loadFunc(LinkedHashMap<String, Operator> funcList) {
            funcList.put("inv", (dst, src) -> {
                if (dst != null) return Result.ERR;
                if (src != null) return Result.ERR;

                getDelayer().inverterEnabled = !getDelayer().inverterEnabled;
                if (getDelayer().inverterEnabled)
                    Util.say(getPlayer(), "info.signal.box.inverter.on");
                else
                    Util.say(getPlayer(), "info.signal.box.inverter.off");

                return Result.OK;
            });
            funcList.put("set", (dst, src) -> {
                if (src != null) return Result.ERR;
                if (dst == null) {
                    if (getDelayer() != null) {
                        prt("[NST] Delay: " + String.valueOf(getDelayer().setTime));
                    }
                } else {
                    if (dst.type != RegType.INT) return Result.ERR;

                    int t = (int) dst.data;
                    if (getDelayer() != null) {
                        getDelayer().setTime = t < 0 ? 0 : t;
                        prt("[NST] Delay: " + String.valueOf(getDelayer().setTime));
                    }
                }

                return Result.OK;
            });
        }
    }

    public BlockDelayer(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityDelayer(type, pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityDelayer delayer && !stack.isEmpty()) {
            ListTag list = Util.getTagListFromNGT(stack);
            if (list == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (!level.isClientSide) {
                String[][] code = NSASM.getCode(list);
                new DelayerConfigurer(code) {
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
                    public TileEntityDelayer getDelayer() {
                        return delayer;
                    }
                }.run();
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
