package club.nsdn.nyasamatelecom.TileEntities;

/**
 * Created by drzzm32 on 2016.5.5.
 */

import club.nsdn.nyasamatelecom.Renderers.TileEntity.*;
import club.nsdn.nyasamatelecom.Renderers.TileEntity.Rail.RailNoSleeperStraightModel;
import club.nsdn.nyasamatelecom.Renderers.TileEntity.Rail.RailRenderer;
import club.nsdn.nyasamatelecom.TileEntities.Rail.RailNoSleeperStraight;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class TileEntityModelBinder {

    public TileEntityModelBinder(FMLInitializationEvent event) {

        ClientRegistry.bindTileEntitySpecialRenderer(
                TileEntityHalfBlock.HalfBlock.class,
                new BaseRenderer(new HalfBlockModel()));

    }

}
