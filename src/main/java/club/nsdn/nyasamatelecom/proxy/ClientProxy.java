package club.nsdn.nyasamatelecom.proxy;

import club.nsdn.nyasamatelecom.event.EventRegister;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamatelecom.tileblock.TileEntityModelBinder;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        new TileEntityModelBinder(event);
        EventRegister.registerClient();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) { super.postInit(event); }


}
