package club.nsdn.nyasamatelecom.tileblock.redstone;

import club.nsdn.nyasamatelecom.api.device.SignalBoxGetter;
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
 * The redstone output box, ported from the 1.12.2 {@code BlockRedOutput} (registry id
 * {@code signal_box_output}).
 *
 * <p>Its state comes from the getter side (the sender's {@code META & 0x8}); what this class adds is the
 * redstone source behaviour. The 1.12.2 hooks map one-to-one onto the 1.21.1 ones:</p>
 * <ul>
 *   <li>{@code canProvidePower(state)} -&gt; {@code isSignalSource(state)}, still true;</li>
 *   <li>{@code getWeakPower(state, access, pos, facing)} -&gt; {@code getSignal(state, level, pos, direction)},
 *       still 15 or 0 from {@code isEnabled};</li>
 *   <li>{@code getStrongPower(...)} -&gt; {@code getDirectSignal(...)}, still 15 only when the queried
 *       direction is {@code UP} (i.e. the box powers the block above it directly);</li>
 *   <li>{@code notifyNeighborsOfStateChange(pos, block, false)} -&gt; {@code level.updateNeighborsAt(pos, block)},
 *       which is the modern spelling of "tell my neighbours to re-read their inputs". The 1.12.2 override
 *       did this for the block itself <b>and</b> the block below, because the strong power is emitted
 *       upwards and the weak power is read from the sides; both calls are preserved.</li>
 * </ul>
 *
 * <p>{@link #refresh()} is called from the block entity's own update loop (the ported
 * {@code SignalBox.TileEntitySignalBox} calls it whenever {@code META} or the enabled flag moves), so the
 * neighbour notification happens exactly where it did in 1.12.2 and nowhere else.</p>
 */
public class BlockRedOutput extends SignalBoxGetter {

    public static class TileEntityRedOutput extends SignalBoxGetter.TileEntitySignalBoxGetter {

        public TileEntityRedOutput(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public void refresh() {
            super.refresh();
            if (level == null) return;
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.updateNeighborsAt(worldPosition.below(), getBlockState().getBlock());
        }
    }

    public BlockRedOutput(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityRedOutput(type, pos, state);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityRedOutput output) {
            return output.isEnabled ? 15 : 0;
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityRedOutput output) {
            if (!output.isEnabled) {
                return 0;
            } else {
                return direction == Direction.UP ? 15 : 0;
            }
        }
        return 0;
    }
}
