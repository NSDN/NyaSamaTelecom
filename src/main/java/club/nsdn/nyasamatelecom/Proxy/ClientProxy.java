package club.nsdn.nyasamatelecom.Proxy;

/**
 * Created by drzzm32 on 2016.5.5.
 */

import club.nsdn.nyasamatelecom.Entity.EntityModelBinder;
import club.nsdn.nyasamatelecom.Entity.MinecartBase;
import club.nsdn.nyasamatelecom.Event.EventRegister;
import club.nsdn.nyasamatelecom.Renderers.Entity.MinecartRenderer;
import club.nsdn.nyasamatelecom.TrainControl.TrainController;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.*;
import club.nsdn.nyasamatelecom.TileEntities.TileEntityModelBinder;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        org.lwjgl.opengl.Display.setTitle("moded Minecraft by NSDN-MC ver1.0");
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
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }


}
