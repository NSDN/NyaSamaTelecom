package club.nsdn.nyasamatelecom.TileEntities;

/**
 * Created by drzzm32 on 2016.5.5.
 */

import club.nsdn.nyasamatelecom.TileEntities.Rail.RailNoSleeperStraight;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class TileEntityLoader {

    public TileEntityLoader(FMLInitializationEvent event) {

        GameRegistry.registerTileEntity(
                TileEntityHalfBlock.HalfBlock.class,
                "tileEntityHalfBlock");

    }

}
