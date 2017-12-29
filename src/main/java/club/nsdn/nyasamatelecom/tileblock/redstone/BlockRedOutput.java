package club.nsdn.nyasamatelecom.tileblock.redstone;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBoxGetter;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import net.minecraft.world.World;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class BlockRedOutput extends SignalBoxGetter {

    public BlockRedOutput() {
        super(NyaSamaTelecom.MODID, "BlockSignalBoxGetter", "signal_box_getter");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    @Override
    public void updateSignal(World world, int x , int y, int z) {
        if (world.getTileEntity(x, y, z) == null) return;
        if (world.getTileEntity(x, y, z) instanceof TileEntitySignalBoxGetter) {
            TileEntitySignalBoxGetter signalBox = (TileEntitySignalBoxGetter) world.getTileEntity(x, y, z);

            int meta = world.getBlockMetadata(x, y, z);
            int old = meta;
            boolean isEnabled;

            if (signalBox.getSender() == null) {
                isEnabled = (meta & 0x8) != 0;
                meta &= 0x7;
            } else {
                isEnabled = signalBox.senderIsPowered();

                if (isEnabled) meta |= 0x8;
                else meta &= 0x7;
            }

            signalBox.isEnabled = isEnabled;

            outputRedstone(world, x, y, z, signalBox.isEnabled);

            if (old != meta) {
                world.markBlockForUpdate(x, y, z);
                world.setBlockMetadataWithNotify(x, y, z, meta, 3);
            }

            world.scheduleBlockUpdate(x, y, z, this, 1);
        }
    }

    public void outputRedstone(World world, int x , int y, int z, boolean state) {

    }

}
