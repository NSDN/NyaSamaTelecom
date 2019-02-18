package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.thewdj.telecom.IReceiver;

/**
 * Created by drzzm32 on 2018.12.13.
 */
public class TileEntityReceiver extends TileEntityBase implements IReceiver<TileEntityTransceiver> {

    public String senderX, senderY, senderZ;

    @Override
    public TileEntityTransceiver getSender() {
        if (senderX.equals("null") || senderY.equals("null") || senderZ.equals("null")) return null;

        TileEntity tileEntity; int x, y, z;
        try {
            x = Integer.parseInt(senderX);
            y = Integer.parseInt(senderY);
            z = Integer.parseInt(senderZ);
        } catch (Exception e) {
            return null;
        }
        tileEntity = getWorld().getTileEntity(new BlockPos(x, y, z));

        if (tileEntity == null) return null;
        if (!(tileEntity instanceof TileEntityTransceiver)) return null;

        return (TileEntityTransceiver) tileEntity;
    }

    @Override
    public void setSender(TileEntityTransceiver sender) {
        if (sender == null) {
            senderX = "null";
            senderY = "null";
            senderZ = "null";
        } else {
            senderX = String.valueOf(sender.getPos().getX());
            senderY = String.valueOf(sender.getPos().getY());
            senderZ = String.valueOf(sender.getPos().getZ());
        }
    }

    @Override
    public boolean senderIsPowered() {
        TileEntityTransceiver sender = getSender();
        if (sender == null) return false;
        return (sender.META & 8) != 0;
    }

    public TileEntityReceiver() {
        senderX = "null";
        senderY = "null";
        senderZ = "null";
    }

    @Override
    public void fromNBT(NBTTagCompound tagCompound) {
        super.fromNBT(tagCompound);
        senderX = tagCompound.getString("senderX");
        senderY = tagCompound.getString("senderY");
        senderZ = tagCompound.getString("senderZ");
    }

    @Override
    public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
        if (getSender() == null) {
            tagCompound.setString("senderX", "null");
            tagCompound.setString("senderY", "null");
            tagCompound.setString("senderZ", "null");
        } else {
            tagCompound.setString("senderX", senderX);
            tagCompound.setString("senderY", senderY);
            tagCompound.setString("senderZ", senderZ);
        }
        return super.toNBT(tagCompound);
    }

    @Override
    public void onDestroy() {
        if (getSender() != null) {
            if (getSender() instanceof TileEntityMultiSender) {
                ((TileEntityMultiSender) getSender()).decTarget();
                getSender().update();
            }
        }
    }

}
