package club.nsdn.nyasamatelecom.tileblock.wireless;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBoxGetter;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityTransceiver;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.thewdj.telecom.IWirelessTx;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class BlockWirelessTx extends SignalBoxGetter {

    public static class TileEntityWirelessTx extends SignalBoxGetter.TileEntitySignalBoxGetter
        implements IWirelessTx<TileEntityTransceiver, TileEntityWirelessTx> {

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
        public TileEntityWirelessTx me() {
            return this;
        }

        @Override
        public boolean getState() {
            return isEnabled;
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
        return new TileEntityWirelessTx();
    }

    public BlockWirelessTx() {
        super(NyaSamaTelecom.MODID, "BlockSignalBoxGetter", "signal_box_getter");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        return false;
    }

}
