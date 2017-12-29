package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class BlockSignalBox extends SignalBox {

    public static class TileEntitySignalBox extends club.nsdn.nyasamatelecom.api.device.SignalBox.TileEntitySignalBox {

        @Override
        public boolean tryControlFirst(boolean state) {
            return false;
        }

        @Override
        public boolean tryControlSecond(boolean state) {
            return false;
        }

    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySignalBox();
    }

    public BlockSignalBox() {
        super(NyaSamaTelecom.MODID, "BlockSignalBox", "signal_box");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}
