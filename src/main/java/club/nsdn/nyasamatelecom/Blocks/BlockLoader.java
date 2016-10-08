package club.nsdn.nyasamatelecom.Blocks;

/**
 * Created by drzzm32 on 2016.5.5.
 */


import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class BlockLoader {

    public static Block blockSign;
    public static Block blockNSDNLogo;
    public static Block blockNyaSamaTelecomLogo;

    private static void register(Block block, String name) {
        GameRegistry.registerBlock(block, name);
    }

    public BlockLoader(FMLPreInitializationEvent event) {
        blockSign = new BlockSign();
        register(blockSign, "nyasamarailway_block_sign");

        blockNSDNLogo = new BlockNSDNLogo();
        register(blockNSDNLogo, "nyasamarailway_nsdn_logo");

        blockNyaSamaTelecomLogo = new BlockNyaSamaTelecomLogo();
        register(blockNyaSamaTelecomLogo, "nyasamarailway_logo");

    }

}