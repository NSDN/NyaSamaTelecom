package club.nsdn.nyasamatelecom.proxy;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.event.EventRegister;
import club.nsdn.nyasamatelecom.network.webservice.ITelecom;
import club.nsdn.nyasamatelecom.network.webservice.TelecomImpl;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class ServerProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);

        NyaSamaTelecom.log.info("Loading WebService...");
        try {
            ITelecom.publish(new TelecomImpl(), "http://0.0.0.0:32/api");
            NyaSamaTelecom.log.info("WebService loaded.");
        } catch (Exception e) {
            NyaSamaTelecom.log.info("WebService failed to load: " + e.getMessage());
        }
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        EventRegister.registerServer();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }


}
