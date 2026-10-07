package club.nsdn.nyasamatelecom.api.device;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * "Getter" signal box, ported from the 1.12.2 {@code SignalBoxGetter}: a receiver-side box whose
 * state is read <i>from</i> its sender. Unlike {@link SignalBox} it does not push anything at a
 * target -- it just mirrors {@code senderIsPowered()} into its own {@code META & 0x8}.
 */
public class SignalBoxGetter extends DeviceBase {

    public static class TileEntitySignalBoxGetter extends TileEntityReceiver {

        public boolean isEnabled;
        boolean prevIsEnabled;

        public TileEntitySignalBoxGetter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            setInfo(8, 0.75, 0.5, 0.75);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntitySignalBoxGetter signalBox)) {
                return;
            }

            int meta = signalBox.META;
            int old = meta;
            boolean isEnabled = signalBox.isEnabled;

            if (signalBox.getSender() != null) {
                isEnabled = signalBox.senderIsPowered();
                signalBox.isEnabled = isEnabled;
            }

            if (isEnabled) {
                meta |= 0x8;
            } else {
                meta &= 0x7;
            }

            if (old != meta || signalBox.isEnabled != signalBox.prevIsEnabled) {
                signalBox.prevIsEnabled = signalBox.isEnabled;
                signalBox.META = meta;
                signalBox.refresh();
            }
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            isEnabled = tagCompound.getBoolean("isEnabled");
            super.fromNBT(tagCompound);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putBoolean("isEnabled", isEnabled);
            return super.toNBT(tagCompound);
        }
    }

    public SignalBoxGetter(BlockBehaviour.Properties properties,
                           Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntitySignalBoxGetter(type, pos, state);
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
