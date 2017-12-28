package club.nsdn.nyasamatelecom.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class BlockSign extends Block {

    public BlockSign() {
        super(Material.glass);
        setBlockName("NyaSamaTelecomBlockSign");
        setBlockTextureName("nyasamatelecom:logo");
        setHardness(2.0F);
        setLightLevel(1);
        setStepSound(Block.soundTypeGlass);
        setResistance(10.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

}
