package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from the 1.12.2 {@code TileEntitySingleSender}: a transceiver that also points at one
 * target device.
 */
public class TileEntitySingleSender extends TileEntityTransceiver {

    public String targetX, targetY, targetZ;

    public TileEntitySingleSender(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        targetX = "null";
        targetY = "null";
        targetZ = "null";
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);
        targetX = tagCompound.getString("targetX");
        targetY = tagCompound.getString("targetY");
        targetZ = tagCompound.getString("targetZ");
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        if (getTarget() == null) {
            tagCompound.putString("targetX", "null");
            tagCompound.putString("targetY", "null");
            tagCompound.putString("targetZ", "null");
        } else {
            tagCompound.putString("targetX", targetX);
            tagCompound.putString("targetY", targetY);
            tagCompound.putString("targetZ", targetZ);
        }
        return super.toNBT(tagCompound);
    }

    public BlockEntity getTarget() {
        if (targetX.equals("null") || targetY.equals("null") || targetZ.equals("null")) {
            return null;
        }

        int x, y, z;
        try {
            x = Integer.parseInt(targetX);
            y = Integer.parseInt(targetY);
            z = Integer.parseInt(targetZ);
        } catch (Exception e) {
            return null;
        }
        if (level == null) {
            return null;
        }
        return level.getBlockEntity(new BlockPos(x, y, z));
    }

    public void setTarget(BlockEntity target) {
        if (target == null) {
            targetX = "null";
            targetY = "null";
            targetZ = "null";
        } else {
            targetX = String.valueOf(target.getBlockPos().getX());
            targetY = String.valueOf(target.getBlockPos().getY());
            targetZ = String.valueOf(target.getBlockPos().getZ());
        }
    }

    /** 1.12.2 called {@code update()} here; 1.21.1's equivalent entry point is {@link #tick()}. */
    public boolean setMetadata(int meta) {
        this.META = meta;
        tick();
        return true;
    }
}
