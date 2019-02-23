package club.nsdn.nyasamatelecom.item;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.renderer.NyaGameMRModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
import org.lwjgl.opengl.Display;

import javax.annotation.Nullable;
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
                return NyaGameMRModel.INSTANCE;
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
        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer; int HEIGHT = renderer.FONT_HEIGHT;
        renderer.drawString("NyaGame MR (TM)", 16, 16, 0x84FFFF);

        renderer.drawString("Tick: " + Minecraft.getMinecraft().getTickLength(), 20, 20 + HEIGHT, 0xEEEEEE);
        renderer.drawString("Render: " + Minecraft.getMinecraft().getRenderPartialTicks(), 20, 20 + HEIGHT * 2, 0xEEEEEE);
        renderer.drawString("Name: " + player.world.getWorldInfo().getWorldName(), 20, 20 + HEIGHT * 3, 0xEEEEEE);
        renderer.drawString("Ver: " + player.world.getWorldInfo().getVersionId(), 20, 20 + HEIGHT * 4, 0xEEEEEE);
        renderer.drawString("Save: " + Long.toHexString(player.world.getWorldInfo().getSizeOnDisk()), 20, 20 + HEIGHT * 5, 0xEEEEEE);
        renderer.drawString("NSDN (C) 2019 | " + Integer.toHexString((int) (65535 * random)).toUpperCase(), 20, 20 + HEIGHT * 6, 0xFFE57F);
    }

}
