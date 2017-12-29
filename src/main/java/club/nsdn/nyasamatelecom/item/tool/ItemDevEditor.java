package club.nsdn.nyasamatelecom.item.tool;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.tool.DevEditor;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;

/**
 * Created by drzzm32 on 2017.12.29.
 */
public class ItemDevEditor extends DevEditor {

    public ItemDevEditor() {
        super(NyaSamaTelecom.MODID, "ItemDevEditor", "item_dev_editor");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }


}
