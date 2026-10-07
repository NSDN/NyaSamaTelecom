package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from the 1.12.2 {@code TileEntityTriStateReceiver}.
 *
 * <p>Three-state logic: {@code +1} / {@code 0} / {@code -1}. The 1.12.2 behaviour that makes this
 * class more than a pair of setters is preserved exactly -- a positive command while the device is in
 * the negative state only returns it to zero (and symmetrically), so opposing signals cancel instead
 * of fighting.</p>
 */
public class TileEntityTriStateReceiver extends TileEntityPassiveReceiver implements ITriStateReceiver {

    public static final int STATE_POS = 1;
    public static final int STATE_ZERO = 0;
    public static final int STATE_NEG = -1;

    public int state;
    public int prevState;

    public TileEntityTriStateReceiver(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setStatePos() {
        state = state == STATE_NEG ? STATE_ZERO : STATE_POS;
    }

    @Override
    public void setStateNeg() {
        state = state == STATE_POS ? STATE_ZERO : STATE_NEG;
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);
        state = tagCompound.getInt("state");
        prevState = tagCompound.getInt("prevState");
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putInt("state", state);
        tagCompound.putInt("prevState", prevState);
        return super.toNBT(tagCompound);
    }
}
