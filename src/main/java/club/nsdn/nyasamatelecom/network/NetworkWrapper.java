package club.nsdn.nyasamatelecom.network;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

/**
 * Created by drzzm32 on 2017.12.28.
 */
public class NetworkWrapper {
    public static SimpleNetworkWrapper instance;

    public NetworkWrapper(FMLPreInitializationEvent event) {
        instance = NetworkRegistry.INSTANCE.newSimpleChannel(NyaSamaTelecom.MODID);
        club.nsdn.nyasamatelecom.api.network.NetworkRegister.register(NyaSamaTelecom.log, instance, 0);
    }
}
