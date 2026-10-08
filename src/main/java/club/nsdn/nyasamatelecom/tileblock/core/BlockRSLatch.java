package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.SignalBoxSender;
import club.nsdn.nyasamatelecom.api.tileentity.ITriStateReceiver;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * The RS latch, ported from the 1.12.2 {@code BlockRSLatch} (registry id {@code rs_latch}).
 *
 * <p>Semantics preserved exactly, including the two things that are easy to "tidy away" by mistake:</p>
 * <ul>
 *   <li>{@code state} is a latch signal that is consumed at the end of every update
 *       ({@code latch.state = STATE_ZERO}) -- it is set by {@link TileEntityRSLatch#setStatePos()} /
 *       {@link TileEntityRSLatch#setStateNeg()} and never persisted beyond its own NBT round trip;</li>
 *   <li>bit {@code 0x8} of {@code META} is still written directly, and a change in either the bit or the
 *       enabled/pending-state pair still triggers {@code refresh()}, so the client re-reads the block
 *       entity.</li>
 * </ul>
 *
 * <p>{@code ITriStateReceiver} is a marker interface in 1.21.1 and in 1.12.2 both -- the two setters below
 * are plain public methods, kept public and unchanged.</p>
 */
public class BlockRSLatch extends SignalBoxSender {

    public static class TileEntityRSLatch extends SignalBoxSender.TileEntitySignalBoxSender
            implements ITriStateReceiver {

        public static final int STATE_POS = 1;
        public static final int STATE_ZERO = 0;
        public static final int STATE_NEG = -1;

        public int state;
        private int prevState;

        public TileEntityRSLatch(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        public void setStatePos() {
            state = state == STATE_NEG ? STATE_ZERO : STATE_POS;
        }

        public void setStateNeg() {
            state = state == STATE_POS ? STATE_ZERO : STATE_NEG;
        }

        public boolean hasChanged() {
            return (prevIsEnabled != isEnabled) || (prevState != state);
        }

        public void updateChanged() {
            prevIsEnabled = isEnabled;
            prevState = state;
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            state = tagCompound.getInt("state");
            super.fromNBT(tagCompound);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putInt("state", state);
            return super.toNBT(tagCompound);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            if (blockEntity instanceof TileEntityRSLatch latch) {

                int meta = latch.META;
                int old = meta;

                switch (latch.state) {
                    case TileEntityRSLatch.STATE_POS:
                        latch.isEnabled = true;
                        break;
                    case TileEntityRSLatch.STATE_NEG:
                        latch.isEnabled = false;
                        break;
                    case TileEntityRSLatch.STATE_ZERO:
                        break;
                    default:
                        break;
                }

                boolean isEnabled = latch.isEnabled;

                if (latch.getTransceiver() != null) {
                    isEnabled = isEnabled && latch.transceiverIsPowered();
                }

                if (isEnabled) meta |= 0x8;
                else meta &= 0x7;

                if (old != meta) {
                    latch.META = meta;
                }
                if (latch.hasChanged()) {
                    latch.updateChanged();
                    latch.refresh();
                }

                latch.state = TileEntityRSLatch.STATE_ZERO;
            }
        }
    }

    public BlockRSLatch(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityRSLatch(type, pos, state);
    }
}
