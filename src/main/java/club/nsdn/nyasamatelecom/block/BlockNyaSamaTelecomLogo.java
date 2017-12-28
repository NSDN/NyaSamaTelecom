package club.nsdn.nyasamatelecom.block;

import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class BlockNyaSamaTelecomLogo extends Block {

    public BlockNyaSamaTelecomLogo() {
        super(Material.glass);
        setBlockName("NyaSamaTelecomLogo");
        setBlockTextureName("nyasamatelecom:nsdn_t_logo");
        setHardness(2.0F);
        setLightLevel(1);
        setStepSound(Block.soundTypeGlass);
        setResistance(10.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}
