package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.SignalBoxGetter;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * The getter signal box, ported from the 1.12.2 {@code BlockSignalBoxGetter}
 * (registry id {@code signal_box_getter}).
 *
 * <p>Empty in 1.12.2 apart from the tile entity subclass and the constructor; all behaviour is in
 * {@code api.device.SignalBoxGetter}.</p>
 */
public class BlockSignalBoxGetter extends SignalBoxGetter {

    public static class TileEntitySignalBoxGetter extends SignalBoxGetter.TileEntitySignalBoxGetter {

        public TileEntitySignalBoxGetter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    public BlockSignalBoxGetter(BlockBehaviour.Properties properties,
                                Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntitySignalBoxGetter(type, pos, state);
    }
}
