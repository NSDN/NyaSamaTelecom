package club.nsdn.nyasamatelecom;

import club.nsdn.nyasamatelecom.item.NyaGameMR;
import club.nsdn.nyasamatelecom.item.tool.ItemConnector;
import club.nsdn.nyasamatelecom.item.tool.ItemDevEditor;
import club.nsdn.nyasamatelecom.item.tool.ItemNGTablet;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registration for {@code nyasamatelecom}.
 *
 * <p>This class <b>replaces</b> the 1.12.2 {@code item.ItemLoader}: a {@code RegistryEvent.Register<Item>}
 * handler over a hand-built {@code LinkedList<Item>}, plus a client-only {@code ModelRegistryEvent}
 * handler that pointed each item at its icon model. The registry event is replaced by the deferred
 * register below and the icon handler by the JSON models under {@code assets/nyasamatelecom/models/item/},
 * so only the four entries survive. Their ids and order are the 1.12.2 ones
 * ({@code connector}, {@code dev_editor}, {@code item_ngt}, {@code nyagame_mr}).</p>
 *
 * <p>The block items are <b>not</b> here -- they are registered next to their blocks in
 * {@link TelecomBlocks#ITEMS}, which is the same split {@code nyasamaoptics} uses.</p>
 */
public final class TelecomItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NyaSamaTelecom.MODID);

    private TelecomItems() {
    }

    public static final DeferredItem<ItemConnector> CONNECTOR =
            ITEMS.register("connector", () -> new ItemConnector(new Item.Properties()));

    public static final DeferredItem<ItemDevEditor> DEV_EDITOR =
            ITEMS.register("dev_editor", () -> new ItemDevEditor(new Item.Properties()));

    public static final DeferredItem<ItemNGTablet> ITEM_NGT =
            ITEMS.register("item_ngt", () -> new ItemNGTablet(new Item.Properties()));

    /**
     * 363 is 1.12.2's helmet durability for {@code ArmorMaterial.DIAMOND}: the enum's base durability 33
     * times the helmet multiplier 11, which {@code ItemArmor} applied for us and which {@link Item.Properties}
     * has to be told explicitly now.
     */
    public static final DeferredItem<NyaGameMR> NYAGAME_MR =
            ITEMS.register("nyagame_mr", () -> new NyaGameMR(new Item.Properties().durability(363)));

    /** Called from {@link NyaSamaTelecom}'s constructor; see {@link TelecomBlocks#register(IEventBus)}. */
    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
