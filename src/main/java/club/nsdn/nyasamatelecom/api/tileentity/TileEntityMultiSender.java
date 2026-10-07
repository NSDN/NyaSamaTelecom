package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from the 1.12.2 {@code TileEntityMultiSender}: a transceiver that tracks how many devices
 * currently point at it ({@code targetCount}), so it can tell whether it still has listeners.
 */
public class TileEntityMultiSender extends TileEntityTransceiver {

    public int targetCount;

    public TileEntityMultiSender(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        targetCount = 0;
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);
        targetCount = tagCompound.getInt("targetCount");
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putInt("targetCount", targetCount);
        return super.toNBT(tagCompound);
    }

    public void incTarget() {
        targetCount += 1;
    }

    public void decTarget() {
        targetCount = targetCount > 0 ? targetCount - 1 : 0;
    }

    public boolean setMetadata(int meta) {
        this.META = meta;
        tick();
        return true;
    }
}
