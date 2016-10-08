package club.nsdn.nyasamatelecom.Proxy;

/**
 * Created by drzzm32 on 2016.5.5.
 */

import club.nsdn.nyasamatelecom.Entity.EntityLoader;
import club.nsdn.nyasamatelecom.Entity.MinecartBase;
import club.nsdn.nyasamatelecom.Event.EventRegister;
import club.nsdn.nyasamatelecom.TrainControl.NetworkWrapper;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamatelecom.Items.ItemLoader;
import club.nsdn.nyasamatelecom.Blocks.BlockLoader;
import club.nsdn.nyasamatelecom.CreativeTab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.TileEntities.TileEntityLoader;
import cpw.mods.fml.common.registry.EntityRegistry;

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
        new EntityLoader(event);
        EventRegister.registerCommon();
    }

    public void postInit(FMLPostInitializationEvent event)
    {

    }

}
