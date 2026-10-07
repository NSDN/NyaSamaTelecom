package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from the 1.12.2 {@code TileEntityTriStateTransmitter}: an actuator that drives a
 * {@link ITriStateReceiver} target, with an optional inverter and a polarity flag
 * ({@code triStateIsNeg}).
 *
 * <p>The {@code prev*} fields are kept even though nothing in the API package reads them yet: the
 * 1.12.2 subclasses in telecom/railway used them for edge detection, so dropping them would silently
 * break those ports. They are deliberately <b>not</b> written to NBT either -- matching 1.12.2, which
 * only persisted {@code triStateIsNeg} and {@code inverterEnabled}.</p>
 */
public class TileEntityTriStateTransmitter extends TileEntityActuator {

    public boolean triStateIsNeg;
    public boolean prevTriStateIsNeg;
    public boolean inverterEnabled;
    public boolean prevInverterEnabled;

    public TileEntityTriStateTransmitter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);
        triStateIsNeg = tagCompound.getBoolean("triStateIsNeg");
        inverterEnabled = tagCompound.getBoolean("inverterEnabled");
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putBoolean("triStateIsNeg", triStateIsNeg);
        tagCompound.putBoolean("inverterEnabled", inverterEnabled);
        return super.toNBT(tagCompound);
    }

    /** Pushes the target to the positive or negative state, honouring the polarity flag. */
    public boolean setSwitch(boolean state) {
        if (getTarget() instanceof ITriStateReceiver) {
            ITriStateReceiver target = (ITriStateReceiver) getTarget();
            if (triStateIsNeg) {
                if (state) {
                    target.setStateNeg();
                }
            } else {
                if (state) {
                    target.setStatePos();
                }
            }
            return true;
        }
        return false;
    }
}
