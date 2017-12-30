package club.nsdn.nyasamatelecom.item;

import club.nsdn.nyasamatelecom.item.tool.*;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.Hashtable;
import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class ItemLoader {

    public static Hashtable<String, Item> items;

    private static void register(Item item, String name) {
        GameRegistry.registerItem(item, name);
    }

    public ItemLoader(FMLPreInitializationEvent event) {
        items = new Hashtable<String, Item>();

        items.put("item_connector", new ItemConnector());
        items.put("item_device_editor", new ItemDevEditor());
        items.put("item_ngtablet", new ItemNGTablet());

        for (String name : items.keySet()) {
            register(items.get(name), name);
        }
    }
}
