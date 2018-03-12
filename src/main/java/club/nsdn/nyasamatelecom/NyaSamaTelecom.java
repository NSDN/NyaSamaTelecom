package club.nsdn.nyasamatelecom;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import club.nsdn.nyasamatelecom.proxy.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by drzzm32 on 2016.10.8.
 */
@Mod(modid = NyaSamaTelecom.MODID, version = NyaSamaTelecom.VERSION)
public class NyaSamaTelecom {

    @Mod.Instance("NyaSamaTelecom")
    public static NyaSamaTelecom instance;
    public static final String MODID = "NyaSamaTelecom";
    public static final String modid = MODID.toLowerCase();
    public static final String VERSION = "0.2";
    public static final boolean isDebug = false;
    public static Logger log = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "club.nsdn.nyasamatelecom.proxy.ClientProxy",
                serverSide = "club.nsdn.nyasamatelecom.proxy.ServerProxy")
    public static CommonProxy proxy;

    public static NyaSamaTelecom getInstance() { return instance; }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }

}
