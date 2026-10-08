package club.nsdn.nyasamatelecom.item;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

/**
 * The NyaGame MR mixed-reality helmet, ported from the 1.12.2 {@code NyaGameMR}.
 *
 * <h2>What the 1.12.2 class was</h2>
 * <p>{@code extends ItemArmor} with {@code (ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.HEAD)}, plus
 * three client-only overrides: a one-line tooltip, {@code getArmorModel(...)} returning the bespoke
 * {@code RenderNyaGameMR.MODEL} biped, {@code getArmorTexture(...)} pointing at
 * {@code textures/item/nyagame_mr.png}, and the interesting one,
 * {@code renderHelmetOverlay(...)}, which drew the heads-up display by scaling to the window height and
 * printing live statistics.</p>
 *
 * <h2>What changed, and why</h2>
 * <ul>
 *   <li>{@code ItemArmor} is now {@link ArmorItem} and its constructor takes a
 *       {@code Holder<ArmorMaterial>} rather than an enum. 1.21.1 armour materials are a datapack registry;
 *       this item instead supplies a <b>direct holder</b> ({@link Holder#direct}) carrying exactly the
 *       1.12.2 diamond values for the helmet slot: 3 defence points, enchantability 10, toughness 2.0,
 *       knockback resistance 0.0, the diamond equip sound and diamond repair ingredient, over a layer
 *       asset id of {@code nyasamatelecom:nyagame_mr}. The old {@code renderIndex} argument (3) has no
 *       successor and is dropped.</li>
 *   <li>{@code getArmorModel} / {@code getArmorTexture} <b>do not exist</b> on {@link ArmorItem} in
 *       1.21.1 (javap-checked): the worn model and its texture now come from client extensions
 *       ({@code IClientItemExtensions#getHumanoidArmorModel}) and from the armour layer's
 *       {@code equipment} asset. Both halves of that moved to
 *       {@code renderer.RenderNyaGameMR}, which owns the helmet model layer and the HUD; this class stays a
 *       plain item. The 1.12.2 tooltip text is kept verbatim.</li>
 *   <li>{@code renderHelmetOverlay(ItemStack, EntityPlayer, ScaledResolution, float)} is gone with the old
 *       GUI stack; the ported HUD is a {@code RegisterGuiLayersEvent} layer, again in
 *       {@code renderer.RenderNyaGameMR}.</li>
 * </ul>
 */
public class NyaGameMR extends ArmorItem {

    /**
     * 1.12.2 {@code ArmorMaterial.DIAMOND} narrowed to the helmet slot. Built as a direct holder because
     * the mod registers no datapack armour material; every number is the value the 1.12.2 enum carried.
     */
    private static final ArmorMaterial MATERIAL = new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET, 3),
            10,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(Items.DIAMOND),
            List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(NyaSamaTelecom.MODID, "nyagame_mr"))),
            2.0F,
            0.0F);

    public NyaGameMR(Item.Properties properties) {
        super(Holder.direct(MATERIAL), ArmorItem.Type.HELMET, properties);
    }

    /** 1.12.2 {@code addInformation}: a single aqua line. Text preserved verbatim. */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.literal("connecting devices").withStyle(ChatFormatting.AQUA));
    }
}
