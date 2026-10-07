package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.thewdj.telecom.ITransceiver;

/**
 * Ported from the 1.12.2 {@code TileEntityTransceiver}.
 *
 * <p>Changes are mechanical: the no-arg constructor becomes the mandatory
 * {@code (BlockEntityType, BlockPos, BlockState)} triple, {@code getWorld().getTileEntity(pos)}
 * becomes {@code level.getBlockEntity(pos)}, {@code getPos()} becomes {@code getBlockPos()}, and the
 * NBT accessors take their 1.21.1 names ({@code getString}/{@code putString}).
 * The peer-tracking semantics (store the peer's coordinates as strings, "null" meaning none, resolve
 * lazily on every access so a broken link self-heals) are kept exactly as they were.</p>
 */
public class TileEntityTransceiver extends TileEntityBase implements ITransceiver<TileEntityTransceiver> {

    public String transceiverX, transceiverY, transceiverZ;

    public TileEntityTransceiver(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        transceiverX = "null";
        transceiverY = "null";
        transceiverZ = "null";
    }

    @Override
    public TileEntityTransceiver getTransceiver() {
        if (transceiverX.equals("null") || transceiverY.equals("null") || transceiverZ.equals("null")) {
            return null;
        }

        int x, y, z;
        try {
            x = Integer.parseInt(transceiverX);
            y = Integer.parseInt(transceiverY);
            z = Integer.parseInt(transceiverZ);
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
    public void setTransceiver(TileEntityTransceiver transceiver) {
        if (transceiver == null) {
            transceiverX = "null";
            transceiverY = "null";
            transceiverZ = "null";
        } else {
            transceiverX = String.valueOf(transceiver.getBlockPos().getX());
            transceiverY = String.valueOf(transceiver.getBlockPos().getY());
            transceiverZ = String.valueOf(transceiver.getBlockPos().getZ());
        }
    }

    @Override
    public boolean transceiverIsPowered() {
        TileEntityTransceiver transceiver = getTransceiver();
        if (transceiver == null) {
            return false;
        }
        return (transceiver.META & 8) != 0;
    }

    @Override
    public void fromNBT(CompoundTag tagCompound) {
        super.fromNBT(tagCompound);
        transceiverX = tagCompound.getString("transceiverX");
        transceiverY = tagCompound.getString("transceiverY");
        transceiverZ = tagCompound.getString("transceiverZ");
    }

    @Override
    public CompoundTag toNBT(CompoundTag tagCompound) {
        if (getTransceiver() == null) {
            tagCompound.putString("transceiverX", "null");
            tagCompound.putString("transceiverY", "null");
            tagCompound.putString("transceiverZ", "null");
        } else {
            tagCompound.putString("transceiverX", transceiverX);
            tagCompound.putString("transceiverY", transceiverY);
            tagCompound.putString("transceiverZ", transceiverZ);
        }
        return super.toNBT(tagCompound);
    }
}
