package club.nsdn.nyasamatelecom.tileblock.wireless;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBoxSender;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityMultiSender;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityTransceiver;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.thewdj.telecom.IWirelessRx;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class BlockWirelessRx extends SignalBoxSender {

    public static class TileEntityWirelessRx extends SignalBoxSender.TileEntitySignalBoxSender
        implements IWirelessRx<TileEntityTransceiver, TileEntityWirelessRx> {

        public String id;
        public String key;

        @Override
        public String id() {
            return null;
        }

        @Override
        public String key() {
            return null;
        }

        @Override
        public TileEntityWirelessRx me() {
            return this;
        }

        @Override
        public void setState(boolean state) {
            this.isEnabled = state;
        }

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            this.id = tagCompound.getString("id");
            this.key = tagCompound.getString("key");
            super.fromNBT(tagCompound);
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
            tagCompound.setString("id", id);
            tagCompound.setString("key", key);
            return super.toNBT(tagCompound);
        }

    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWirelessRx();
    }

    public BlockWirelessRx() {
        super(NyaSamaTelecom.MODID, "BlockSignalBoxSender", "signal_box_sender");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        return false;
    }

}
