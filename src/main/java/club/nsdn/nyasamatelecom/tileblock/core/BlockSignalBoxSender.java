package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.SignalBoxSender;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * The sender signal box, ported from the 1.12.2 {@code BlockSignalBoxSender}
 * (registry id {@code signal_box_sender}).
 *
 * <p>The 1.12.2 class body was empty apart from the tile entity subclass and the constructor: the
 * click-to-toggle interaction lives in {@code api.device.SignalBoxSender}, which is already ported. The
 * tile entity subclass exists only so that this block gets its own {@code BlockEntityType} and therefore
 * its own renderer binding, exactly as the 1.12.2 {@code TileEntityModelBinder} bound
 * {@code TileEntitySignalBoxSender} to a buttoned {@code SignalBoxRenderer}.</p>
 */
public class BlockSignalBoxSender extends SignalBoxSender {

    public static class TileEntitySignalBoxSender extends SignalBoxSender.TileEntitySignalBoxSender {

        public TileEntitySignalBoxSender(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    public BlockSignalBoxSender(BlockBehaviour.Properties properties,
                                Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntitySignalBoxSender(type, pos, state);
    }
}
