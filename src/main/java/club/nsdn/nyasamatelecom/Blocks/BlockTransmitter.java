package club.nsdn.nyasamatelecom.Blocks;

/**
 * Created by drzzm32 on 2016.10.10.
 */

import club.nsdn.nyasamatelecom.CreativeTab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.Event.TelecomCore;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import org.thewdj.telecom.ITransmitter;

import java.util.Random;

public class BlockTransmitter extends Block implements ITransmitter {

    public BlockTransmitter() {
        super(Material.glass);
        setBlockName("BlockTransmitter");
        setBlockTextureName("nyasamatelecom:block_tx");
        setHardness(1.0F);
        setLightLevel(0);
        setStepSound(Block.soundTypeGlass);
        setResistance(5.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    @Override
    public Object[] collectData() {

        return null;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {

    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        TelecomCore.instance().registerTransmitter(this, "");
    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
        TelecomCore.instance().disposeTransmitter("");
    }

}
