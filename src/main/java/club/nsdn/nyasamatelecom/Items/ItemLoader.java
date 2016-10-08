package club.nsdn.nyasamatelecom.Items;

/**
 * Created by drzzm32 on 2016.5.5.
 */

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ItemLoader {



    private static void register(Item item, String name) {
        GameRegistry.registerItem(item, name);
    }

    public ItemLoader(FMLPreInitializationEvent event) {

    }
}
