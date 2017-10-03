package club.nsdn.nyasamatelecom.Blocks;

/**
 * Created by drzzm32 on 2016.10.10.
 */

import club.nsdn.nyasamatelecom.CreativeTab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import java.util.Random;

public class BlockDebugger extends Block {

    public BlockDebugger() {
        super(Material.glass);
        setBlockName("debugger");
        setBlockTextureName("nyasamatelecom:debugger");
        setHardness(1.0F);
        setLightLevel(0);
        setStepSound(Block.soundTypeGlass);
        setResistance(5.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {

    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {

    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {

    }

}
