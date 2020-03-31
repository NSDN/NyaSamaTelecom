package club.nsdn.nyasamatelecom.api.util;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/**
 * Created by drzzm32 on 2017.12.28.
 */
public class Util {

    public static class Color3 {
        public float r, g, b;
    }

    public static Color3 hsv2RGB(float h, float s, float v) {
        h = h % 360;
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = v - c;
        float rt, gt, bt;
        if (h >= 0 && h < 60) {
            rt = c; gt = x; bt = 0;
        } else if (h >= 60 && h < 120) {
            rt = x; gt = c; bt = 0;
        } else if (h >= 120 && h < 180) {
            rt = 0; gt = c; bt = x;
        } else if (h >= 180 && h < 240) {
            rt = 0; gt = x; bt = c;
        } else if (h >= 240 && h < 300) {
            rt = x; gt = 0; bt = c;
        } else if (h >= 300 && h < 360) {
            rt = c; gt = 0; bt = x;
        } else {
            rt = gt = bt = 0;
        }
        Color3 color = new Color3();
        color.r = rt + m;
        color.g = gt + m;
        color.b = bt + m;
        return color;
    }

    public static NBTTagList getTagListFromNGT(ItemStack itemStack) {
        if (itemStack == null) return null;
        if (itemStack.getItem() instanceof NGTablet) {
            NBTTagCompound tagCompound = itemStack.getTagCompound();
            if (tagCompound == null) return null;
            if (tagCompound.hasKey("code")) {
                String code = tagCompound.getString("code");
                NBTTagList list = new NBTTagList();
                list.appendTag(new NBTTagString(code));
                return list;
            }
            return tagCompound.getTagList("pages", 8);
        }
        return null;
    }

    public static void say(EntityPlayer player, String format, Object... args) {
        if (player == null) return;
        if (player instanceof EntityPlayerMP)
            player.sendMessage(new TextComponentTranslation(format, args));
    }

    public static void setStateWithTile(World world, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            IBlockState old = world.getBlockState(pos);
            world.setBlockState(pos, state, 3);
            world.notifyBlockUpdate(pos, old, state, 2);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

}
