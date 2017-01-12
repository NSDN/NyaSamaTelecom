package club.nsdn.nyasamatelecom.Proxy;

/**
 * Created by drzzm32 on 2016.10.8.
 */

import club.nsdn.nyasamatelecom.Entity.EntityModelBinder;
import club.nsdn.nyasamatelecom.Event.EventRegister;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamatelecom.TileEntities.TileEntityModelBinder;

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
        new EntityModelBinder(event);
        EventRegister.registerClient();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) { super.postInit(event); }


}
