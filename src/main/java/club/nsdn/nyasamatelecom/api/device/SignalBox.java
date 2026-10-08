package club.nsdn.nyasamatelecom.api.device;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityActuator;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import cn.ac.nya.telecom.IPassive;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * Signal box, ported from the 1.12.2 {@code SignalBox}. This is the heart of the telecom API: it
 * converts "is my sender powered / is my link alive" into bit {@code 0x8} of its own {@code META},
 * then pushes that state at whatever target is wired to it.
 *
 * <p>Structurally unchanged from 1.12.2, including the parts that are easy to lose in a port:</p>
 * <ul>
 *   <li><b>{@code CONTROL_FUNCS}</b> -- a static {@code LinkedList<IControlFunc>} that other mods
 *       register into. It is the documented extension point of this API package (a device type that
 *       needs custom control logic registers a function here instead of subclassing the signal box),
 *       so it is preserved verbatim, as is the {@code IPassive} special case: a passive receiver's
 *       target is only driven through {@code controlTarget} if it is not an {@code IPassive}.</li>
 *   <li><b>Placement is floor/wall only</b> -- unlike {@link DeviceBase}, the 1.12.2 signal boxes had
 *       just two pitch branches (no ceiling case, no {@code +8}). Overriding {@code setPlacedBy} to
 *       keep that is deliberate, not an oversight.</li>
 *   <li>{@code breakBlock} -> {@code onRemove}, so {@code onDestroy()} still runs and a multi-sender
 *       peer still gets its target count decremented.</li>
 * </ul>
 */
public class SignalBox extends DeviceBase {

    /** The signal box's block entity; also the place third-party control functions hook into. */
    public static class TileEntitySignalBox extends TileEntityActuator {

        @FunctionalInterface
        public interface IControlFunc {
            void control(BlockEntity blockEntity, boolean state);
        }

        public static LinkedList<IControlFunc> CONTROL_FUNCS = new LinkedList<>();

        public boolean isEnabled;
        public boolean inverterEnabled;
        public boolean prevInverterEnabled;

        public TileEntitySignalBox(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            setInfo(8, 0.75, 0.5, 0.75);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntitySignalBox signalBox)) {
                return;
            }

            int meta = signalBox.META;
            int old = meta;
            boolean isEnabled;

            if (signalBox.getSender() == null) {
                isEnabled = (meta & 0x8) != 0;
                meta &= 0x7;
            } else {
                isEnabled = signalBox.senderIsPowered();
                if (isEnabled) {
                    meta |= 0x8;
                } else {
                    meta &= 0x7;
                }
            }

            signalBox.isEnabled = isEnabled;

            if (signalBox.inverterEnabled) {
                isEnabled = !isEnabled;
            }

            if (!signalBox.tryControlFirst(isEnabled)
                    && !signalBox.tryControlSecond(isEnabled)
                    && !signalBox.setTargetSender(isEnabled)
                    && !signalBox.setTargetGetter(isEnabled)) {
                if (signalBox.getTarget() != null) {
                    blockEntity = signalBox.getTarget();
                    for (IControlFunc func : CONTROL_FUNCS) {
                        func.control(blockEntity, isEnabled);
                    }
                    if (blockEntity instanceof TileEntityReceiver && blockEntity instanceof IPassive) {
                        signalBox.controlTarget(isEnabled);
                    } else {
                        signalBox.controlTarget(isEnabled);
                    }
                }
            }

            if (old != meta || signalBox.prevInverterEnabled != signalBox.inverterEnabled) {
                signalBox.prevInverterEnabled = signalBox.inverterEnabled;
                signalBox.META = meta;
                signalBox.refresh();
            }
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            super.fromNBT(tagCompound);
            isEnabled = tagCompound.getBoolean("isEnabled");
            inverterEnabled = tagCompound.getBoolean("inverterEnabled");
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putBoolean("isEnabled", isEnabled);
            tagCompound.putBoolean("inverterEnabled", inverterEnabled);
            return super.toNBT(tagCompound);
        }

        /** Subclass hooks, kept so downstream devices can intercept before the generic path. */
        public boolean tryControlFirst(boolean state) {
            return false;
        }

        public boolean tryControlSecond(boolean state) {
            return false;
        }

        public boolean setTargetSender(boolean state) {
            BlockEntity target = getTarget();
            if (target instanceof SignalBoxSender.TileEntitySignalBoxSender sender) {
                sender.isEnabled = state;
                return true;
            }
            return false;
        }

        public boolean setTargetGetter(boolean state) {
            BlockEntity target = getTarget();
            if (target instanceof SignalBoxGetter.TileEntitySignalBoxGetter getter) {
                getter.isEnabled = state;
                return true;
            }
            return false;
        }
    }

    public SignalBox(BlockBehaviour.Properties properties,
                     Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntitySignalBox(type, pos, state);
    }

    /** Floor/wall only -- see the class note about the missing ceiling case. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        int val = net.minecraft.util.Mth.floor((double) (placer.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
        if (placer.getXRot() > 22.5F) {
            setDeviceMeta(level, pos, val);
        } else {
            setDeviceMeta(level, pos, val + 4);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityReceiver receiver) {
            receiver.onDestroy();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return false;
    }
}
