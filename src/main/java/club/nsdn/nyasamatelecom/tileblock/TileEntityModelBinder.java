package club.nsdn.nyasamatelecom.tileblock;

import club.nsdn.nyasamatelecom.renderer.*;
import club.nsdn.nyasamatelecom.tileblock.core.*;
import club.nsdn.nyasamatelecom.tileblock.redstone.*;
import club.nsdn.nyasamatelecom.tileblock.wireless.*;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class TileEntityModelBinder {

    public static LinkedHashMap<Class<? extends TileEntity>, TileEntitySpecialRenderer> renderers;

    private static void bind(TileEntitySpecialRenderer renderer, Class<? extends TileEntity> tileEntity) {
        ClientRegistry.bindTileEntitySpecialRenderer(tileEntity, renderer);
    }

    public TileEntityModelBinder(FMLInitializationEvent event) {
        renderers = new LinkedHashMap<Class<? extends TileEntity>, TileEntitySpecialRenderer>();

        renderers.put(BlockSignalBox.TileEntitySignalBox.class, new SignalBoxRenderer(false));
        renderers.put(BlockSignalBoxSender.TileEntitySignalBoxSender.class, new SignalBoxRenderer(true));
        renderers.put(BlockSignalBoxGetter.TileEntitySignalBoxGetter.class, new SignalBoxRenderer(false, "signal_box_gt"));
        renderers.put(BlockTriStateSignalBox.TileEntityTriStateSignalBox.class, new TriStateSignalBoxRenderer());

        renderers.put(BlockRedInput.TileEntitySignalBoxSender.class, new SignalBoxRenderer(true, "signal_box_redstone"));
        renderers.put(BlockRedOutput.TileEntitySignalBoxGetter.class, new SignalBoxRenderer(false, "signal_box_redstone"));

        renderers.put(BlockWirelessRx.TileEntityWirelessRx.class, new SignalBoxRenderer(true, "signal_box_wireless"));
        renderers.put(BlockWirelessTx.TileEntityWirelessTx.class, new SignalBoxRenderer(false, "signal_box_wireless"));

        for (Class<? extends TileEntity> tile : renderers.keySet()) {
            bind(renderers.get(tile), tile);
        }
    }

}
