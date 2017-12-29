package club.nsdn.nyasamatelecom.tileblock;

import club.nsdn.nyasamatelecom.tileblock.core.*;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.tileentity.TileEntity;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class TileEntityLoader {

    public static LinkedHashMap<String, Class<? extends TileEntity>> tileEntities;

    private static void register(Class<? extends TileEntity> tileEntity, String name) {
        GameRegistry.registerTileEntity(tileEntity, name);
    }

    public TileEntityLoader(FMLInitializationEvent event) {
        tileEntities = new LinkedHashMap<String, Class<? extends TileEntity>>();

        tileEntities.put("tileSignalBox", BlockSignalBox.TileEntitySignalBox.class);
        tileEntities.put("tileSignalBoxSender", BlockSignalBoxSender.TileEntitySignalBoxSender.class);
        tileEntities.put("tileSignalBoxGetter", BlockSignalBoxGetter.TileEntitySignalBoxGetter.class);
        tileEntities.put("tileTriStateSignalBox", BlockTriStateSignalBox.TileEntityTriStateSignalBox.class);

        for (String name : tileEntities.keySet()) {
            register(tileEntities.get(name), name);
        }
    }

}
