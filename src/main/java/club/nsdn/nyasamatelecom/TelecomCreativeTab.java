package club.nsdn.nyasamatelecom;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The single creative tab of {@code nyasamatelecom}, replacing the 1.12.2
 * {@code creativetab.CreativeTabLoader}.
 *
 * <p>The old class was an anonymous {@code CreativeTabs} subclass whose only overridden member was
 * {@code getTabIconItem()}. 1.21.1 tabs are built with {@code CreativeModeTab.builder()}
 * (the anonymous-subclass form disappeared in 1.18) and the item list comes from
 * {@code displayItems} instead of the tab's implicit "every item whose creative tab is me" rule, so the
 * list below walks the whole module in the 1.12.2 registration order:
 * {@code BlockLoader}'s 20 blocks first, then {@code ItemLoader}'s 4 items.</p>
 *
 * <p>The translation key is the 1.12.2 one, {@code itemGroup.tabNyaSamaTelecom}, unchanged -- that is this
 * project's convention for the ported tabs (see {@code itemGroup.tabNyaSamaOptics} in
 * {@code nyasamaoptics}). The icon is the same block item the 1.12.2 tab used: {@code BlockLoader.logo},
 * i.e. {@code nst_logo}.</p>
 */
public final class TelecomCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NyaSamaTelecom.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("tab_nyasama_telecom", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tabNyaSamaTelecom"))
                    .icon(() -> new ItemStack(TelecomBlocks.NST_LOGO.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(TelecomBlocks.NST_LOGO.get());
                        output.accept(TelecomBlocks.NST_SIGN.get());
                        output.accept(TelecomBlocks.NST_NSDN.get());

                        output.accept(TelecomBlocks.NSPGA_T0C0.get());
                        output.accept(TelecomBlocks.NSPGA_T4C4.get());
                        output.accept(TelecomBlocks.NSPGA_F011.get());
                        output.accept(TelecomBlocks.NSPGA_F211.get());
                        output.accept(TelecomBlocks.NSPGA_F344.get());

                        output.accept(TelecomBlocks.NSASM_BOX.get());
                        output.accept(TelecomBlocks.SIGNAL_BOX.get());
                        output.accept(TelecomBlocks.SIGNAL_BOX_SENDER.get());
                        output.accept(TelecomBlocks.SIGNAL_BOX_GETTER.get());
                        output.accept(TelecomBlocks.TRI_STATE_SIGNAL_BOX.get());

                        output.accept(TelecomBlocks.SIGNAL_BOX_INPUT.get());
                        output.accept(TelecomBlocks.SIGNAL_BOX_OUTPUT.get());

                        output.accept(TelecomBlocks.SIGNAL_BOX_RX.get());
                        output.accept(TelecomBlocks.SIGNAL_BOX_TX.get());

                        output.accept(TelecomBlocks.RS_LATCH.get());
                        output.accept(TelecomBlocks.TIMER.get());
                        output.accept(TelecomBlocks.DELAYER.get());

                        output.accept(TelecomItems.CONNECTOR.get());
                        output.accept(TelecomItems.DEV_EDITOR.get());
                        output.accept(TelecomItems.ITEM_NGT.get());
                        output.accept(TelecomItems.NYAGAME_MR.get());
                    })
                    .build());

    private TelecomCreativeTab() {
    }

    /** Called from {@link NyaSamaTelecom}'s constructor; see {@link TelecomBlocks#register(IEventBus)}. */
    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
