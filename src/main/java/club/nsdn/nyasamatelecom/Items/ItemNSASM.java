package club.nsdn.nyasamatelecom.Items;

import club.nsdn.nyasamatelecom.GUI.GuiPause;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import club.nsdn.nyasamatelecom.GUI.JavaFx.InputBox;

/**
 * Created by drzzm on 2017.3.9.
 */

public class ItemNSASM extends ItemToolBase {

    public ItemNSASM() {
        super(ToolMaterial.IRON);
        setTexName("item_nsasm");
        setUnlocalizedName("ItemNSASM");
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, final EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz) {
        if (!world.isRemote) return true;

        showGUI(player);

        return true;
    }

    @SideOnly(Side.CLIENT)
    public void showGUI(EntityPlayer player) {
        InputBox.getInstance().show(obj -> {
            if (obj != null) {
                if (obj instanceof String) {
                    System.out.println(obj);
                }
            }
            player.closeScreen();
        });

        Minecraft.getMinecraft().displayGuiScreen(new GuiPause(() -> {
            InputBox.getInstance().hide();
        }));
    }

}
