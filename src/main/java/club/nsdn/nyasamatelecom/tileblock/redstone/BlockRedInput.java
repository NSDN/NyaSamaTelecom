package club.nsdn.nyasamatelecom.tileblock.redstone;

import club.nsdn.nyasamatelecom.api.device.SignalBoxSender;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * The redstone input box, ported from the 1.12.2 {@code BlockRedInput} (registry id
 * {@code signal_box_input}).
 *
 * <p>The output stage is inherited ({@code SignalBoxSender}: {@code isEnabled && transceiverIsPowered()}
 * into {@code META & 0x8}); only the input side changed:
 * {@code world.isBlockIndirectlyGettingPowered(pos) > 0} is
 * {@code level.hasNeighborSignal(pos)} in 1.21.1 (verified on {@code SignalGetter}, which {@code Level}
 * implements) and has the same "any neighbour is powering me, including a redstone block" meaning.</p>
 *
 * <p>{@code canConnectRedstone(IBlockState, IBlockAccess, BlockPos, EnumFacing)} becomes the modern
 * {@code canConnectRedstone(BlockState, BlockGetter, BlockPos, Direction)} and still returns true -- which
 * is the whole point of this block. The 1.12.2 {@code onBlockActivated} override that unconditionally
 * returned {@code false} has no 1.21.1 counterpart to write: returning
 * {@code PASS_TO_DEFAULT_BLOCK_INTERACTION} from nothing is already the default, so the override is
 * dropped rather than written as a no-op (noted here so the missing method is not read as an omission).</p>
 */
public class BlockRedInput extends SignalBoxSender {

    public static class TileEntityRedInput extends SignalBoxSender.TileEntitySignalBoxSender {

        public TileEntityRedInput(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityRedInput redInput) {

                redInput.isEnabled = inputRedstone(level, pos);

                int meta = redInput.META;
                int old = meta;
                boolean isEnabled = redInput.isEnabled;

                if (redInput.getTransceiver() != null) {
                    isEnabled = isEnabled && redInput.transceiverIsPowered();
                }

                if (isEnabled) meta |= 0x8;
                else meta &= 0x7;

                if (old != meta || redInput.prevIsEnabled != redInput.isEnabled) {
                    redInput.prevIsEnabled = redInput.isEnabled;
                    redInput.META = meta;
                    redInput.refresh();
                }
            }
        }

        public boolean inputRedstone(Level level, BlockPos pos) {
            return level.hasNeighborSignal(pos);
        }
    }

    public BlockRedInput(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityRedInput(type, pos, state);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }
}
