package club.nsdn.nyasamatelecom.Blocks;

/**
 * Created by drzzm32 on 2016.10.8.
 */

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.LinkedHashMap;

public class BlockLoader {

    public static LinkedHashMap<String, Block> blocks;
    public static Block logo;

    private static void register(Block block, String name) {
        GameRegistry.registerBlock(block, name);
    }

    public BlockLoader(FMLPreInitializationEvent event) {
        blocks = new LinkedHashMap<String, Block>();

        logo = new BlockNyaSamaTelecomLogo();
        blocks.put("nst_BlockNyaSamaTelecomLogo", logo);

        blocks.put("nst_BlockSign", new BlockSign());
        blocks.put("nst_BlockNSDNLogo", new BlockNSDNLogo());
        //blocks.put("BlockTransmitter", new BlockTransmitter());
        //blocks.put("BlockReceiver", new BlockReceiver());
        //blocks.put("BlockDebugger", new BlockDebugger());

        for (String name : blocks.keySet()) {
            register(blocks.get(name), name);
        }

    }

}