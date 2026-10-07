package club.nsdn.nyasamatelecom.api.util;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from the 1.12.2 {@code club.nsdn.nyasamatelecom.api.util.Util}.
 *
 * <p>{@link #hsv2RGB} is carried over verbatim -- it is pure math and feeds the rendered
 * signal-connection colours, so any change would be a visible behaviour change.</p>
 *
 * <p>Note for later: {@code Color3}/{@code hsv2RGB} are version-neutral and are therefore a natural
 * candidate to move into the {@code core} module (like {@code DataSheets} and the NSASM/NSPGA
 * algorithms before them). They are kept here for now so this pass stays a faithful port of the
 * package; the move is recorded as a follow-up rather than done silently.</p>
 *
 * <p><b>No longer deferred:</b> {@code getTagListFromNGT(ItemStack)} is now implemented against the
 * {@code CUSTOM_DATA} component, now that the {@code NGTablet} item exists. (The 1.12.2 version read
 * item NBT directly; 1.21.1 moved item data to components.)</p>
 */
public class Util {

    public static class Color3 {
        public float r, g, b;
    }

    /** Verbatim from 1.12.2. */
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

    /**
     * Ported from the 1.12.2 {@code getTagListFromNGT}. No longer deferred: the {@code NGTablet} item
     * now exists, so the {@code instanceof} test is the real one.
     *
     * <p>The 1.12.2 version read {@code itemStack.getTagCompound()} and returned either a one-entry
     * list built from the {@code code} string or the {@code pages} list. In 1.21.1 that data lives in
     * the {@code CUSTOM_DATA} component, so the tag is taken from there; the shape of the result is
     * unchanged.</p>
     */
    public static ListTag getTagListFromNGT(ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof NGTablet)) {
            return null;
        }
        CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        net.minecraft.nbt.CompoundTag tag = data.copyTag();
        if (tag.contains("code")) {
            String code = tag.getString("code");
            ListTag list = new ListTag();
            list.add(StringTag.valueOf(code));
            return list;
        }
        return tag.getList("pages", net.minecraft.nbt.Tag.TAG_STRING);
    }

    /** Action-bar message; 1.12.2 used a {@code TextComponentTranslation} to a server player only. */
    public static void say(Player player, String format, Object... args) {
        if (player == null) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(format, args), false);
        }
    }

    /**
     * Set the block and refresh clients. 1.12.2 used
     * {@code setBlockState(..., 3)} + {@code notifyBlockUpdate} + {@code markBlockRangeForRenderUpdate};
     * 1.21.1 collapses the last two into {@code sendBlockUpdated}.
     */
    public static void setStateWithTile(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) == null) {
            return;
        }
        BlockState old = level.getBlockState(pos);
        level.setBlock(pos, state, 3);
        level.sendBlockUpdated(pos, old, state, 2);
    }
}
