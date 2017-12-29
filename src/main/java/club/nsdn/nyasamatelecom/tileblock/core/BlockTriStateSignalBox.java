package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.TriStateSignalBox;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class BlockTriStateSignalBox extends TriStateSignalBox {

    public BlockTriStateSignalBox() {
        super(NyaSamaTelecom.MODID, "BlockTriStateSignalBox", "tri_state_signal_box");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}
