package club.nsdn.nyasamatelecom.Blocks;

/**
 * Created by drzzm32 on 2016.10.10.
 */

import club.nsdn.nyasamatelecom.CreativeTab.CreativeTabLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import org.thewdj.telecom.IRegister;

import java.util.Random;

public class BlockDebugger extends Block implements IRegister {

    public BlockDebugger() {
        super(Material.glass);
        setBlockName("Debugger");
        setBlockTextureName("nyasamatelecom:debugger");
        setHardness(1.0F);
        setLightLevel(0);
        setStepSound(Block.soundTypeGlass);
        setResistance(5.0F);
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    public Object[] getData() {
        return new String[]{"1", "2", "3", "4", "5", "6"};
    }

    public Object getData(int index) {
        return String.valueOf(index);
    }

    public void setData(Object[] data) {
        int i = 1;
        for (Object o : data) {
            System.out.println("[NST DEBUG] " + i + "->" + o.toString());
            i += 1;
        }
    }

    public void setData(Object data, int index) {
        System.out.println("[NST DEBUG] " + index + "->" + data.toString());
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
