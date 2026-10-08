package club.nsdn.nyasamatelecom.item.tool;

import club.nsdn.nyasamatelecom.api.tool.DevEditor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;

/**
 * The device editor tool, ported from the 1.12.2 {@code ItemDevEditor}
 * ({@code super(NyaSamaTelecom.MODID, "ItemDevEditor", "dev_editor")} plus a creative tab).
 *
 * <p>See {@link ItemConnector} for the constructor mapping. Note that {@code DevEditor} keeps the 1.12.2
 * quirk that its two bookkeeping maps are {@code static} but are re-created by every constructor -- so this
 * class being instantiated by the deferred register re-initialises the shared maps, exactly as the 1.12.2
 * loader did.</p>
 */
public class ItemDevEditor extends DevEditor {

    public ItemDevEditor(Item.Properties properties) {
        super(properties, Tiers.IRON);
    }
}
