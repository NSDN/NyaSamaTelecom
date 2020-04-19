package club.nsdn.nyasamatelecom.proxy;

import club.nsdn.nyasamatelecom.api.tool.util.NGTCommand;
import club.nsdn.nyasamatelecom.event.EventRegister;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.*;

/**
 * Created by drzzm32 on 2018.12.12.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        EventRegister.registerClient();
        ClientCommandHandler.instance.registerCommand(new NGTCommand());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) { super.postInit(event); }


}
