package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBoxGetter;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class BlockSignalBoxGetter extends SignalBoxGetter {

    public BlockSignalBoxGetter() {
        super(NyaSamaTelecom.MODID, "BlockSignalBoxGetter", "signal_box_getter");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}
