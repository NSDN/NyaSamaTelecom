package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import cn.ac.nya.telecom.IPassive;
import cn.ac.nya.telecom.IRelay;

/**
 * Ported from the 1.12.2 {@code TileEntityActuator}: a receiver that drives one target device by
 * flipping bit {@code 0x8} of its {@code META} -- the framework's "powered" convention that
 * {@code transceiverIsPowered()}/{@code senderIsPowered()} read back.
 */
public class TileEntityActuator extends TileEntityReceiver implements IRelay<BlockEntity, TileEntityTransceiver>, IPassive {

    public String targetX, targetY, targetZ;

    public TileEntityActuator(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        targetX = "null";
        targetY = "null";
        targetZ = "null";
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        targetX = tagCompound.getString("targetX");
        targetY = tagCompound.getString("targetY");
        targetZ = tagCompound.getString("targetZ");
        super.fromNBT(tagCompound);
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

    @Override
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

    @Override
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

    @Override
    public void controlTarget(boolean state) {
        int meta = getTargetMetadata();
        if (meta < 0) {
            return;
        }
        setTargetMetadata((state && (meta & 0x8) == 0) ? meta | 0x8 : meta);
    }

    public int getTargetMetadata() {
        BlockEntity target = getTarget();
        if (target instanceof TileEntityBase) {
            return ((TileEntityBase) target).META;
        }
        return -1;
    }

    public boolean setTargetMetadata(int meta) {
        BlockEntity target = getTarget();
        if (target instanceof TileEntityBase) {
            ((TileEntityBase) target).META = meta;
            ((TileEntityBase) target).refresh();
            return true;
        }
        return false;
    }
}
