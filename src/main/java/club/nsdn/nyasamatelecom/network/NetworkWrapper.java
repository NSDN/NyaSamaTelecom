package club.nsdn.nyasamatelecom.network;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.network.NetworkRegister;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by drzzm32 on 2018.12.13.
 */
public class NetworkWrapper {
    public static SimpleNetworkWrapper instance;

    public NetworkWrapper(FMLPreInitializationEvent event) {
        instance = NetworkRegistry.INSTANCE.newSimpleChannel(NyaSamaTelecom.MODID);
        int pos = NetworkRegister.register(NyaSamaTelecom.logger, instance, 0);
        NSPGAPacket.logger = NyaSamaTelecom.logger;
        instance.registerMessage(NSPGAPacketHandler.class, NSPGAPacket.class, pos, Side.SERVER);
    }
}
