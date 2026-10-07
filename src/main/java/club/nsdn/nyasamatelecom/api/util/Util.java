package club.nsdn.nyasamatelecom.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
 * <p><b>Deferred:</b> 1.12.2 also had {@code getTagListFromNGT(ItemStack)}, which reads the page list
 * off an {@code NGTablet} item. The tablet lives in the {@code api/tool} package (not ported yet) and
 * item NBT moved to data components in 1.21.1, so that method is left out until the tool package is
 * ported.</p>
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
