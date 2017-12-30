package club.nsdn.nyasamatelecom.block;

import club.nsdn.nyasamatelecom.tileblock.core.*;
import club.nsdn.nyasamatelecom.tileblock.redstone.*;
import club.nsdn.nyasamatelecom.tileblock.wireless.*;
import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.Hashtable;
import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class BlockLoader {

    public static Hashtable<String, Block> blocks;
    public static Block logo;

    private static void register(Block block, String name) {
        GameRegistry.registerBlock(block, name);
    }

    public BlockLoader(FMLPreInitializationEvent event) {
        blocks = new Hashtable<String, Block>();

        logo = new BlockNyaSamaTelecomLogo();
        blocks.put("nst_logo", logo);

        blocks.put("nst_sign", new BlockSign());
        blocks.put("nst_nsdn", new BlockNSDNLogo());

        blocks.put("signal_box", new BlockSignalBox());
        blocks.put("signal_box_sender", new BlockSignalBoxSender());
        blocks.put("signal_box_getter", new BlockSignalBoxGetter());
        blocks.put("tri_state_signal_box", new BlockTriStateSignalBox());

        blocks.put("signal_box_input", new BlockRedInput());
        blocks.put("signal_box_output", new BlockRedOutput());

        blocks.put("signal_box_rx", new BlockWirelessRx());
        blocks.put("signal_box_tx", new BlockWirelessTx());

        for (String name : blocks.keySet()) {
            register(blocks.get(name), name);
        }
    }

}