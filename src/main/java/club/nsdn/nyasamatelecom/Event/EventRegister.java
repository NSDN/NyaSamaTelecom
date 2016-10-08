package club.nsdn.nyasamatelecom.Event;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created by drzzm32 on 2016.5.13.
 */

public class EventRegister {

    public static void registerCommon() {
        MinecraftForge.EVENT_BUS.register(ToolHandler.instance());
        ForgeChunkManager.setForcedChunkLoadingCallback(NyaSamaTelecom.getInstance(), ChunkLoaderHandler.instance());
        MinecraftForge.EVENT_BUS.register(ChunkLoaderHandler.instance());
    }

    public static void registerServer() {

    }

    public static void registerClient() {
        ;
    }

}
