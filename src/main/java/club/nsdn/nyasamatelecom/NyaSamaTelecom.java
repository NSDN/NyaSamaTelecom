package club.nsdn.nyasamatelecom;

/**
 * Created by drzzm32 on 2016.10.8.
 */

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import club.nsdn.nyasamatelecom.Proxy.CommonProxy;

import java.io.PrintStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

@Mod(modid = NyaSamaTelecom.MODID, version = NyaSamaTelecom.VERSION)
public class NyaSamaTelecom {

    @Mod.Instance("NyaSamaTelecom")
    public static NyaSamaTelecom instance;
    public static final String MODID = "NyaSamaTelecom";
    public static final String VERSION = "0.1";
    public static final boolean isDebug = false;
    public static PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));

    @SidedProxy(clientSide = "club.nsdn.nyasamatelecom.Proxy.ClientProxy",
                serverSide = "club.nsdn.nyasamatelecom.Proxy.ServerProxy")
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
