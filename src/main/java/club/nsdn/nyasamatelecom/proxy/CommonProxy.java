package club.nsdn.nyasamatelecom.proxy;

import club.nsdn.nyasamatelecom.event.EventRegister;
import club.nsdn.nyasamatelecom.network.NetworkWrapper;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamatelecom.item.ItemLoader;
import club.nsdn.nyasamatelecom.block.BlockLoader;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.tileblock.TileEntityLoader;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event)
    {
        new NetworkWrapper(event);
        new CreativeTabLoader(event);
        new ItemLoader(event);
        new BlockLoader(event);
    }

    public void init(FMLInitializationEvent event)
    {
        new TileEntityLoader(event);
        EventRegister.registerCommon();
    }

    public void postInit(FMLPostInitializationEvent event)
    {

    }

}
