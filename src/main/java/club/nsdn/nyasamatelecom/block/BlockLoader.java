package club.nsdn.nyasamatelecom.block;

import club.nsdn.nyasamatelecom.tileblock.core.*;
import club.nsdn.nyasamatelecom.tileblock.redstone.*;
import club.nsdn.nyasamatelecom.tileblock.wireless.*;
import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import cn.ac.nya.nspga.*;

import java.util.Hashtable;

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

        blocks.put("nspga_t0c0", new BlockNSPGA(NSPGAT0C0.class, "BlockNSPGAT0C0", "nspga_t0c0i8o8r0"));
        blocks.put("nspga_t4c4", new BlockNSPGA(NSPGAT4C4.class, "BlockNSPGAT4C4", "nspga_t4c4i8o8r0"));

        blocks.put("nsasm_box", new BlockNSASMBox());
        blocks.put("signal_box", new BlockSignalBox());
        blocks.put("signal_box_sender", new BlockSignalBoxSender());
        blocks.put("signal_box_getter", new BlockSignalBoxGetter());
        blocks.put("tri_state_signal_box", new BlockTriStateSignalBox());

        blocks.put("signal_box_input", new BlockRedInput());
        blocks.put("signal_box_output", new BlockRedOutput());

        blocks.put("signal_box_rx", new BlockWirelessRx());
        blocks.put("signal_box_tx", new BlockWirelessTx());

        blocks.put("rs_latch_box", new BlockRSLatch());
        blocks.put("timer_box", new BlockTimer());
        blocks.put("delayer_box", new BlockDelayer());

        for (String name : blocks.keySet()) {
            register(blocks.get(name), name);
        }
    }

}