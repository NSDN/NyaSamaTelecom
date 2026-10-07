package club.nsdn.nyasamatelecom.api.tool;

import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;

/**
 * NGT tablet tool, ported from the 1.12.2 {@code NGTablet}.
 *
 * <p>Two structural changes:</p>
 * <ul>
 *   <li>1.12.2 extended {@code ItemWritableBook} so the tablet could hold book pages. In 1.21.1 the
 *       writable-book behaviour is assembled from data components and that class no longer exists as a
 *       base to extend, so the tablet is a plain {@link Item} whose pages are read out of the
 *       {@code CUSTOM_DATA} component by {@link Util#getTagListFromNGT}. The pages themselves are
 *       synced by {@code NGTPacket}, which now tests {@code instanceof NGTablet} directly -- the
 *       temporary shim from the network port is gone.</li>
 *   <li>The {@code SimpleNetworkWrapper} constructor parameter and {@code getNetworkWrapper()} hook are
 *       removed: sending is {@code PacketDistributor}-based in NeoForge, so the tool no longer needs to
 *       be handed a channel. Subclasses across the mods lose a constructor argument.</li>
 * </ul>
 *
 * <p>The script path is unchanged in behaviour: sneaking runs the stored code through the telecom
 * NSASM dialect (adding the {@code rnd} function), otherwise the GUI opens. {@code posY + 1.5} is kept
 * as the script origin height, matching 1.12.2.</p>
 */
public class NGTablet extends Item {

    private final String modid;

    public NGTablet(Item.Properties properties, String modid) {
        super(properties.stacksTo(1));
        this.modid = modid.toLowerCase();
    }

    public String getModId() {
        return modid;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            runNGT(stack, level, player);
        } else {
            showGUI(stack, level, player);
        }
        return InteractionResultHolder.success(stack);
    }

    public void runNGT(ItemStack itemStack, Level level, Player player) {
        if (!player.isShiftKeyDown()) {
            return;
        }
        ListTag list = Util.getTagListFromNGT(itemStack);
        if (list == null) {
            return;
        }
        String[][] code = NSASM.getCode(list);
        new NSASM(code) {
            @Override
            public Level getWorld() {
                return level;
            }

            @Override
            public double getX() {
                return player.getX();
            }

            @Override
            public double getY() {
                return player.getY() + 1.5;
            }

            @Override
            public double getZ() {
                return player.getZ();
            }

            @Override
            public Player getPlayer() {
                return player;
            }

            @Override
            public void loadFunc(LinkedHashMap<String, Operator> funcList) {
                funcList.put("rnd", (dst, src) -> {
                    if (src != null || dst == null) {
                        return Result.ERR;
                    }
                    if (dst.readOnly) {
                        return Result.ERR;
                    }
                    dst.type = RegType.INT;
                    dst.data = Math.round(Math.random() * 255);
                    return Result.OK;
                });
            }
        }.run();
    }

    /**
     * Client half of the tablet interaction: opens the in-game editor.
     *
     * <p>1.12.2 called {@code Minecraft.getMinecraft().displayGuiScreen(new GuiNGTablet(...))} here.
     * The call now goes through {@code NGTabletClient}, a client-only class, because this class is
     * also loaded on a dedicated server and must not carry a {@code Minecraft} reference itself.</p>
     */
    public void showGUI(ItemStack itemStack, Level level, Player player) {
        if (!player.isShiftKeyDown()) {
            club.nsdn.nyasamatelecom.api.tool.util.NGTabletClient.openEditor(modid, itemStack);
        }
    }
}
