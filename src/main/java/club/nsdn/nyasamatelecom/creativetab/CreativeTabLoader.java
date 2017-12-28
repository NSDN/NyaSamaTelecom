package club.nsdn.nyasamatelecom.creativetab;

import net.minecraft.item.Item;
import cpw.mods.fml.common.event.*;
import net.minecraft.creativetab.CreativeTabs;
import club.nsdn.nyasamatelecom.block.BlockLoader;

/**
 * Created by drzzm32 on 2016.10.8.
 */
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
