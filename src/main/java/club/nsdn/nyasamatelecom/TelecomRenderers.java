package club.nsdn.nyasamatelecom;

import club.nsdn.nyasamatelecom.renderer.AdvancedBoxRenderer;
import club.nsdn.nyasamatelecom.renderer.DeviceModels;
import club.nsdn.nyasamatelecom.renderer.NSPGAFlexRenderer;
import club.nsdn.nyasamatelecom.renderer.NSPGARenderer;
import club.nsdn.nyasamatelecom.renderer.RenderNyaGameMR;
import club.nsdn.nyasamatelecom.renderer.SignalBoxRenderer;
import club.nsdn.nyasamatelecom.renderer.TriStateSignalBoxRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Client-side registration for the telecom renderers, models and helmet HUD.
 *
 * <p>This class <b>replaces</b> the 1.12.2 {@code tileblock.TileEntityModelBinder}, which bound a
 * {@code TileEntitySpecialRenderer} to each tile entity class from a client-only
 * {@code ModelRegistryEvent} handler. Two things have to happen here that did not exist then:</p>
 * <ul>
 *   <li>{@link EntityRenderersEvent.RegisterRenderers} binds a <b>block entity type</b> to its renderer
 *       (the 1.12.2 mapping was by tile entity class), which is why the fourteen bindings below name the
 *       types from {@link TelecomBlockEntities} rather than the classes. The mapping itself is the 1.12.2
 *       one, including every {@code SignalBoxRenderer} constructor argument -- the ones with a button
 *       ({@code signal_box_sender}, {@code signal_box_input}, {@code signal_box_rx}, {@code rs_latch},
 *       {@code timer}) and the ones with a named texture ({@code nsasm_box_base},
 *       {@code signal_box_gt}, {@code signal_box_redstone}, {@code signal_box_wireless},
 *       {@code delayer_base}).</li>
 *   <li>{@link ModelEvent.RegisterAdditional} is required because these models are addressed at runtime
 *       instead of being referenced from a blockstate (decision D4). NeoForge requires the
 *       {@code standalone} variant, and the same {@link ModelResourceLocation} must be used later to fetch
 *       the baked model -- which is exactly what {@link DeviceModels} does, so its catalogue drives both
 *       sides and no renderer has to be grepped.</li>
 * </ul>
 *
 * <p>The helmet adds three more mod-bus registrations, all of which 1.12.2 expressed differently: a model
 * layer definition (the old {@code ModelBiped} was just constructed), a client item extension (the old
 * {@code ItemArmor#getArmorModel} override) and a GUI layer (the old
 * {@code ItemArmor#renderHelmetOverlay}).</p>
 *
 * <p>Why the annotation rather than a call from {@link NyaSamaTelecom}: all five of these events are mod-bus
 * events, the class is client-only, and {@code @EventBusSubscriber} is the project's established way of
 * expressing that (see {@code OpticsRenderers}). The block/item/block-entity registration layer, by
 * contrast, <b>is</b> called explicitly from the entry constructor -- that distinction is deliberate and is
 * recorded in {@link NyaSamaTelecom}'s javadoc.</p>
 */
@EventBusSubscriber(modid = NyaSamaTelecom.MODID, value = Dist.CLIENT)
public final class TelecomRenderers {

    private TelecomRenderers() {
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // The three renderers with no 1.12.2 constructor arguments still need the provider's Context
        // parameter, because BlockEntityRendererProvider's factory method takes it.
        bind(event, TelecomBlockEntities.NSPGA.get(), context -> new NSPGARenderer());
        bind(event, TelecomBlockEntities.NSPGA_FLEX.get(), context -> new NSPGAFlexRenderer());

        bind(event, TelecomBlockEntities.NSASM_BOX.get(),
                context -> new SignalBoxRenderer(false, "nsasm_box_base"));
        bind(event, TelecomBlockEntities.SIGNAL_BOX.get(),
                context -> new SignalBoxRenderer(false));
        bind(event, TelecomBlockEntities.SIGNAL_BOX_SENDER.get(),
                context -> new SignalBoxRenderer(true));
        bind(event, TelecomBlockEntities.SIGNAL_BOX_GETTER.get(),
                context -> new SignalBoxRenderer(false, "signal_box_gt"));
        bind(event, TelecomBlockEntities.TRI_STATE_SIGNAL_BOX.get(),
                context -> new TriStateSignalBoxRenderer());

        bind(event, TelecomBlockEntities.SIGNAL_BOX_INPUT.get(),
                context -> new SignalBoxRenderer(true, "signal_box_redstone"));
        bind(event, TelecomBlockEntities.SIGNAL_BOX_OUTPUT.get(),
                context -> new SignalBoxRenderer(false, "signal_box_redstone"));

        bind(event, TelecomBlockEntities.SIGNAL_BOX_RX.get(),
                context -> new SignalBoxRenderer(true, "signal_box_wireless"));
        bind(event, TelecomBlockEntities.SIGNAL_BOX_TX.get(),
                context -> new SignalBoxRenderer(false, "signal_box_wireless"));

        bind(event, TelecomBlockEntities.RS_LATCH.get(),
                context -> new AdvancedBoxRenderer(true, "rs_latch_base"));
        bind(event, TelecomBlockEntities.TIMER.get(),
                context -> new AdvancedBoxRenderer(true, "timer_base"));
        bind(event, TelecomBlockEntities.DELAYER.get(),
                context -> new SignalBoxRenderer(false, "delayer_base"));
    }

    /**
     * Binds a type to a renderer despite the wildcard.
     *
     * <p>{@code registerBlockEntityRenderer} is generic in the block entity type while the telecom types are
     * held as {@code BlockEntityType<?>} (the deferred registers erase their element type), so the compiler
     * cannot infer {@code T}. This helper performs the one unavoidable unchecked cast in a single place
     * instead of scattering casts through the list; the cast is safe by construction, since each provider's
     * renderer parameter type matches the block entity class registered for that type.</p>
     */
    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> void bind(EntityRenderersEvent.RegisterRenderers event,
                                                     BlockEntityType<?> type,
                                                     BlockEntityRendererProvider<T> provider) {
        event.registerBlockEntityRenderer((BlockEntityType<T>) type, provider);
    }

    @SubscribeEvent
    static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation id : DeviceModels.catalogue()) {
            event.register(ModelResourceLocation.standalone(id));
        }
    }

    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        RenderNyaGameMR.registerLayerDefinitions(event);
    }

    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        RenderNyaGameMR.registerClientExtensions(event);
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        RenderNyaGameMR.registerGuiLayer(event);
    }
}
