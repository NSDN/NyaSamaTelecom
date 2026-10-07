package club.nsdn.nyasamatelecom.api.device;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityTriStateTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Three-state signal box, ported from the 1.12.2 {@code TriStateSignalBox}.
 *
 * <p>Same shape as the other signal boxes, but the output stage is
 * {@link TileEntityTriStateTransmitter#setSwitch(boolean)} rather than {@code controlTarget}, so the
 * target is pushed to its positive/negative state instead of being switched on/off. The 1.12.2
 * "if (!setSwitch(...)) { int i = 1; }" no-op branch is dropped -- it was dead code (an unused local)
 * and porting it verbatim would only add noise.</p>
 */
public class TriStateSignalBox extends DeviceBase {

    public static class TileEntityTriStateSignalBox extends TileEntityTriStateTransmitter {

        public TileEntityTriStateSignalBox(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            setInfo(8, 0.75, 0.5, 0.75);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntityTriStateSignalBox signalBox)) {
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

            if (signalBox.inverterEnabled) {
                isEnabled = !isEnabled;
            }

            signalBox.setSwitch(isEnabled);

            if (old != meta
                    || signalBox.prevInverterEnabled != signalBox.inverterEnabled
                    || signalBox.prevTriStateIsNeg != signalBox.triStateIsNeg) {
                signalBox.prevInverterEnabled = signalBox.inverterEnabled;
                signalBox.prevTriStateIsNeg = signalBox.triStateIsNeg;
                signalBox.META = meta;
                signalBox.refresh();
            }
        }
    }

    public TriStateSignalBox(BlockBehaviour.Properties properties,
                             Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityTriStateSignalBox(type, pos, state);
    }

    /** Floor/wall only, as in 1.12.2. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        int val = Mth.floor((double) (placer.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
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
