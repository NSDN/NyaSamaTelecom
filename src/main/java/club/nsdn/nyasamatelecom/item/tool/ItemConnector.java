package club.nsdn.nyasamatelecom.item.tool;

import club.nsdn.nyasamatelecom.api.tool.Connector;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;

/**
 * The connector tool, ported from the 1.12.2 {@code ItemConnector}.
 *
 * <p>The 1.12.2 constructor was the whole class body:
 * {@code super(NyaSamaTelecom.MODID, "ItemConnector", "connector")} plus
 * {@code setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom)}. In 1.21.1 the mod id / unlocalised name /
 * registry name triple is gone -- the registry name is the {@code DeferredItem}'s id in
 * {@code TelecomItems} and the creative tab is declared by the tab itself -- so what is left is the two
 * things {@code ToolBase} still needs: the item properties and the tool material.
 * {@code Material.IRON} became {@link Tiers#IRON}, and {@code ToolBase(properties, tier)} applies the old
 * {@code setMaxStackSize(1)} / {@code setMaxDamage(tier.getUses())} pair itself, so no properties are set
 * here.</p>
 */
public class ItemConnector extends Connector {

    public ItemConnector(Item.Properties properties) {
        super(properties, Tiers.IRON);
    }
}
