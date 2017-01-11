package club.nsdn.nyasamatelecom.Blocks;

/**
 * Created by drzzm32 on 2016.10.8.
 */

import club.nsdn.nyasamatelecom.CreativeTab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockNSDNLogo extends Block {

    public BlockNSDNLogo() {
        super(Material.glass);
        setBlockName("NyaSamaTelecomNSDNLogo");
        setBlockTextureName("nyasamatelecom:nsdn_logo");
        setHardness(2.0F);
        setLightLevel(1);
        setStepSound(Block.soundTypeGlass);
        setResistance(10.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}
