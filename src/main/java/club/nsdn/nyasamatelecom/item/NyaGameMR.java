package club.nsdn.nyasamatelecom.item;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.renderer.RenderNyaGameMR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by drzzm32 on 2019.2.23.
 */
public class NyaGameMR extends ItemArmor {

    public NyaGameMR() {
        super(ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.HEAD);
        setUnlocalizedName("NyaGameMR");
        setRegistryName(NyaSamaTelecom.MODID, "nyagame_mr");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        list.add(TextFormatting.AQUA + "connecting devices");
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nullable
    public ModelBiped getArmorModel(EntityLivingBase player, ItemStack stack, EntityEquipmentSlot slot, ModelBiped biped) {
        if (!stack.isEmpty())
            if (stack.getItem() == this)
                return RenderNyaGameMR.MODEL;
        return null;
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        if (!stack.isEmpty())
            if (stack.getItem() == this)
                return "nyasamatelecom:textures/item/nyagame_mr.png";
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float random) {
        RenderNyaGameMR.INSTANCE.drawHUDBack();

        RenderNyaGameMR.INSTANCE.clear();
        RenderNyaGameMR.INSTANCE.print("NyaGame MR (TM) by NSDN (R)");
        RenderNyaGameMR.INSTANCE.print("  ");
        RenderNyaGameMR.INSTANCE.print("Hello, %s", Minecraft.getMinecraft().getSession().getUsername());
        RenderNyaGameMR.INSTANCE.print("  ");
        String[] array = Minecraft.getMinecraft().getVersion().split("-");
        RenderNyaGameMR.INSTANCE.print("Ver: %s", array[array.length - 1]);
        RenderNyaGameMR.INSTANCE.print("Tick:   %1.2f ms", Minecraft.getMinecraft().getTickLength());
        RenderNyaGameMR.INSTANCE.print("Render: %1.2f ms", Minecraft.getMinecraft().getRenderPartialTicks());
        double vx = player.posX - player.prevPosX, vy = player.posY - player.prevPosY, vz = player.posZ - player.prevPosZ;
        double v = Math.sqrt(vx * vx + vy * vy + vz * vz);
        RenderNyaGameMR.INSTANCE.print("  ");
        RenderNyaGameMR.INSTANCE.print("Motion: %1.2f m/tick", v);
        RenderNyaGameMR.INSTANCE.print("Speed:  %1.2f m/s", v * 20);
        RenderNyaGameMR.INSTANCE.print("Vel:    %1.2f km/h", v * 20 * 3.6);
        RenderNyaGameMR.INSTANCE.print("  ");
        RenderNyaGameMR.INSTANCE.print("[%s]", Long.toHexString((long) (4294967295.0 * (double) random)).toUpperCase());

        RenderNyaGameMR.INSTANCE.drawHUDString();
    }

}
