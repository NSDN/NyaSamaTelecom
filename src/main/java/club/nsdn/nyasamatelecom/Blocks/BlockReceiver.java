package club.nsdn.nyasamatelecom.Blocks;

/**
 * Created by drzzm32 on 2016.10.10.
 */

import club.nsdn.nyasamatelecom.CreativeTab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import org.thewdj.telecom.IReceiver;

import java.util.Random;

public class BlockReceiver extends Block {

    public BlockReceiver() {
        super(Material.glass);
        setBlockName("BlockReceiver");
        setBlockTextureName("nyasamatelecom:block_rx");
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
