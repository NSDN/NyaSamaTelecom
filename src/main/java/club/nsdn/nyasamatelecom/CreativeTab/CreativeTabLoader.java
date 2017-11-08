package club.nsdn.nyasamatelecom.CreativeTab;

/**
 * Created by drzzm32 on 2016.10.8.
 */

import net.minecraft.item.Item;
import cpw.mods.fml.common.event.*;
import net.minecraft.creativetab.CreativeTabs;
import club.nsdn.nyasamatelecom.Blocks.BlockLoader;

public class CreativeTabLoader {

    public static CreativeTabs tabNyaSamaTelecom;

    public CreativeTabLoader(FMLPreInitializationEvent event) {
        tabNyaSamaTelecom = new CreativeTabs("tabNyaSamaTelecom") {
            @Override
            public Item getTabIconItem() {
                return Item.getItemFromBlock(BlockLoader.logo);
            }
        };
    }

}
