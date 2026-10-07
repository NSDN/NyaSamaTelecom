package club.nsdn.nyasamatelecom.api.network;

import net.minecraft.world.item.ItemStack;

/**
 * Placeholder for the not-yet-ported {@code api/tool/NGTablet} item.
 *
 * <p>Why this exists: {@link NGTPacket#handle} needs to know whether a stack is an NGT tablet, but the
 * tool package (tablet, connector, editors, GUIs -- roughly 900 lines) is ported later. Rather than
 * weakening the packet to "any item with NBT" -- which would silently change who can receive the sync
 * -- the test is isolated here and currently always returns {@code false}, so the handler is inert
 * until the tool package lands and this becomes {@code stack.getItem() instanceof NGTablet}.</p>
 *
 * <p>Deleting this shim is part of the {@code api/tool} port; the call site does not change.</p>
 */
public final class NGTabletShim {

    private NGTabletShim() {
    }

    public static boolean isTablet(ItemStack stack) {
        return false;
    }
}
