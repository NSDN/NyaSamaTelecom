package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBoxSender;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class BlockSignalBoxSender extends SignalBoxSender {

    public BlockSignalBoxSender() {
        super(NyaSamaTelecom.MODID, "BlockSignalBoxSender", "signal_box_sender");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}
