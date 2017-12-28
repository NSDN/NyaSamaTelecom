package club.nsdn.nyasamatelecom.block;

import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * Created by drzzm32 on 2016.10.8.
 */
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
