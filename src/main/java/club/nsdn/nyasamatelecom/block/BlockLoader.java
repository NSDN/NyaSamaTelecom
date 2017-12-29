package club.nsdn.nyasamatelecom.block;

import club.nsdn.nyasamatelecom.tileblock.core.*;
import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class BlockLoader {

    public static LinkedHashMap<String, Block> blocks;
    public static Block logo;

    private static void register(Block block, String name) {
        GameRegistry.registerBlock(block, name);
    }

    public BlockLoader(FMLPreInitializationEvent event) {
        blocks = new LinkedHashMap<String, Block>();

        logo = new BlockNyaSamaTelecomLogo();
        blocks.put("nst_logo", logo);

        blocks.put("nst_sign", new BlockSign());
        blocks.put("nst_nsdn", new BlockNSDNLogo());

        blocks.put("signal_box", new BlockSignalBox());
        blocks.put("signal_box_sender", new BlockSignalBoxSender());
        blocks.put("signal_box_getter", new BlockSignalBoxGetter());
        blocks.put("tri_state_signal_box", new BlockTriStateSignalBox());

        for (String name : blocks.keySet()) {
            register(blocks.get(name), name);
        }
    }

}