package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import cn.ac.nya.telecom.IReceiver;

/**
 * Ported from the 1.12.2 {@code TileEntityReceiver}.
 *
 * <p>Same mechanical changes as {@link TileEntityTransceiver}. Note the 1.12.2
 * {@code onDestroy()} bookkeeping (telling a {@code TileEntityMultiSender} peer that one of its
 * targets went away) is preserved; in 1.21.1 {@link #onDestroy()} is called from the block entity's
 * removal hook by the concrete device's {@code BlockEntityType}, so the contract is unchanged.</p>
 */
public class TileEntityReceiver extends TileEntityBase implements IReceiver<TileEntityTransceiver> {

    public String senderX, senderY, senderZ;

    public TileEntityReceiver(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        senderX = "null";
        senderY = "null";
        senderZ = "null";
    }

    @Override
    public TileEntityTransceiver getSender() {
        if (senderX.equals("null") || senderY.equals("null") || senderZ.equals("null")) {
            return null;
        }

        int x, y, z;
        try {
            x = Integer.parseInt(senderX);
            y = Integer.parseInt(senderY);
            z = Integer.parseInt(senderZ);
        } catch (Exception e) {
            return null;
        }
        if (level == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(new BlockPos(x, y, z));

        if (!(blockEntity instanceof TileEntityTransceiver)) {
            return null;
        }
        return (TileEntityTransceiver) blockEntity;
    }

    @Override
    public void setSender(TileEntityTransceiver sender) {
        if (sender == null) {
            senderX = "null";
            senderY = "null";
            senderZ = "null";
        } else {
            senderX = String.valueOf(sender.getBlockPos().getX());
            senderY = String.valueOf(sender.getBlockPos().getY());
            senderZ = String.valueOf(sender.getBlockPos().getZ());
        }
    }

    @Override
    public boolean senderIsPowered() {
        TileEntityTransceiver sender = getSender();
        if (sender == null) {
            return false;
        }
        return (sender.META & 8) != 0;
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);
        senderX = tagCompound.getString("senderX");
        senderY = tagCompound.getString("senderY");
        senderZ = tagCompound.getString("senderZ");
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        if (getSender() == null) {
            tagCompound.putString("senderX", "null");
            tagCompound.putString("senderY", "null");
            tagCompound.putString("senderZ", "null");
        } else {
            tagCompound.putString("senderX", senderX);
            tagCompound.putString("senderY", senderY);
            tagCompound.putString("senderZ", senderZ);
        }
        return super.toNBT(tagCompound);
    }

    @Override
    public void onDestroy() {
        if (getSender() != null && getSender() instanceof TileEntityMultiSender) {
            ((TileEntityMultiSender) getSender()).decTarget();
            getSender().tick();
        }
    }
}
