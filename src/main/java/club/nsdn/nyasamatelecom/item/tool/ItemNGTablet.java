package club.nsdn.nyasamatelecom.item.tool;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.network.NetworkWrapper;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class ItemNGTablet extends NGTablet {

    public ItemNGTablet() {
        super(NyaSamaTelecom.modid, NetworkWrapper.instance, "ItemNGTablet", "item_ngt");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}
