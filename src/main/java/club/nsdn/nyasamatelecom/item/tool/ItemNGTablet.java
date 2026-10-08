package club.nsdn.nyasamatelecom.item.tool;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import net.minecraft.world.item.Item;

/**
 * The NGT terminal item, ported from the 1.12.2 {@code ItemNGTablet}.
 *
 * <p>1.12.2 passed its {@code SimpleNetworkWrapper} into the constructor twice -- once as a
 * {@code super(...)} argument and once as a {@code getNetworkWrapper()} override. Both are gone: the ported
 * {@code NGTablet} routes its editor through {@code NGTabletClient} and the payload API, so only the item
 * properties and the mod id (used to pick the terminal's own data keys) survive.</p>
 */
public class ItemNGTablet extends NGTablet {

    public ItemNGTablet(Item.Properties properties) {
        super(properties, NyaSamaTelecom.MODID);
    }
}
