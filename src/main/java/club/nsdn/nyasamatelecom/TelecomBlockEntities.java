package club.nsdn.nyasamatelecom;

import club.nsdn.nyasamatelecom.tileblock.core.BlockDelayer;
import club.nsdn.nyasamatelecom.tileblock.core.BlockNSASMBox;
import club.nsdn.nyasamatelecom.tileblock.core.BlockNSPGA;
import club.nsdn.nyasamatelecom.tileblock.core.BlockNSPGAFlex;
import club.nsdn.nyasamatelecom.tileblock.core.BlockRSLatch;
import club.nsdn.nyasamatelecom.tileblock.core.BlockSignalBox;
import club.nsdn.nyasamatelecom.tileblock.core.BlockSignalBoxGetter;
import club.nsdn.nyasamatelecom.tileblock.core.BlockSignalBoxSender;
import club.nsdn.nyasamatelecom.tileblock.core.BlockTimer;
import club.nsdn.nyasamatelecom.tileblock.core.BlockTriStateSignalBox;
import club.nsdn.nyasamatelecom.tileblock.redstone.BlockRedInput;
import club.nsdn.nyasamatelecom.tileblock.redstone.BlockRedOutput;
import club.nsdn.nyasamatelecom.tileblock.wireless.BlockWirelessRx;
import club.nsdn.nyasamatelecom.tileblock.wireless.BlockWirelessTx;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block entity types for {@code nyasamatelecom}.
 *
 * <p>This class <b>replaces</b> the 1.12.2 {@code tileblock.TileEntityLoader}. 1.12.2 registered nothing
 * but a name for each tile entity class ({@code GameRegistry.registerTileEntity(cls, id)}); the game then
 * created one by calling the class's no-arg constructor. 1.21.1 needs a real
 * {@link BlockEntityType} per entity class, which is also what carries the ticker, so the 14 types below
 * are the direct descendants of the 14 {@code tileEntities.add(...)} lines in that loader.</p>
 *
 * <h2>Why the factory cannot be {@code Class::new}</h2>
 * <p>{@code BlockEntityType.Builder.of(BlockEntitySupplier<T>, Block...)} and
 * {@code BlockEntitySupplier<T>} is {@code T create(BlockPos, BlockState)} -- verified with javap against
 * {@code neoforge-21.1.256-merged.jar}: the <b>type is not passed in</b>, unlike the
 * {@code BlockEntity(BlockEntityType, BlockPos, BlockState)} constructor. Each lambda therefore resolves
 * its own type through a <b>static accessor</b> on this class
 * (e.g. {@code BlockSignalBox.TileEntitySignalBox(signalBoxType(), pos, state)}); the accessor reads the
 * {@link DeferredHolder} lazily, long after registration, which is what makes the self-reference legal.
 * Writing the holder's field directly inside the supplier would be a self-reference in an initialiser and
 * would be rejected by javac.</p>
 *
 * <p>A single type is shared by a family only where 1.12.2 also shared one tile entity class:
 * {@code nspga} covers the two {@code BlockNSPGA} blocks and {@code nspga_flex} the three
 * {@code BlockNSPGAFlex} blocks. Both read their driver class back off
 * {@code getBlockState().getBlock()} at runtime, exactly as 1.12.2 read {@code getBlockType()}.</p>
 *
 * <h2>Chunk-loading ticket controller</h2>
 * <p>1.12.2 registered a {@code ForgeChunkManager} loading callback
 * ({@code ForgeChunkManager.setForcedChunkLoadingCallback}) from {@code event.EventRegister}. Both the
 * callback type and {@code ForgeChunkManager.Ticket} are gone; the modern mechanism is a
 * {@link TicketController} registered through {@link RegisterTicketControllersEvent} (a <b>mod bus</b>
 * event), and registering is not optional -- {@code ForcedChunkManager.forceChunk} throws for an
 * unregistered controller and a saved ticket of one is silently discarded on load. {@link #ANCHOR} is that
 * controller. The 1.12.2 {@code ChunkLoaderHandler#ticketsLoaded} callback itself has no counterpart to
 * port (NeoForge's forced chunks are persisted as {@code (controller id, chunk, owner)} and reinstated by
 * the level itself), which is why {@code event.ChunkLoaderHandler} carries that note instead of an
 * equivalent.</p>
 */
public final class TelecomBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NyaSamaTelecom.MODID);

    /** The mod-scoped id the chunk-loading tickets of the wireless boxes and the flex NSPGA are keyed by. */
    public static final ResourceLocation ANCHOR_ID =
            ResourceLocation.fromNamespaceAndPath(NyaSamaTelecom.MODID, "anchor");

    /**
     * 1.12.2: {@code ForgeChunkManager.setForcedChunkLoadingCallback(mod, ChunkLoaderHandler.instance())}.
     * A {@code TicketController} is a record keyed by its {@code ResourceLocation}, so this instance and
     * any other one built from {@link #ANCHOR_ID} are interchangeable.
     */
    public static final TicketController ANCHOR = new TicketController(ANCHOR_ID);

    private TelecomBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(TelecomBlockEntities::registerTicketControllers);
    }

    private static void registerTicketControllers(RegisterTicketControllersEvent event) {
        event.register(ANCHOR);
    }

    /* -------- one type per 1.12.2 tile entity class -------- */

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> SIGNAL_BOX =
            BLOCK_ENTITIES.register("signal_box", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockSignalBox.TileEntitySignalBox(signalBoxType(), pos, state),
                            TelecomBlocks.SIGNAL_BOX.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> SIGNAL_BOX_SENDER =
            BLOCK_ENTITIES.register("signal_box_sender", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockSignalBoxSender.TileEntitySignalBoxSender(
                                    signalBoxSenderType(), pos, state),
                            TelecomBlocks.SIGNAL_BOX_SENDER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> SIGNAL_BOX_GETTER =
            BLOCK_ENTITIES.register("signal_box_getter", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockSignalBoxGetter.TileEntitySignalBoxGetter(
                                    signalBoxGetterType(), pos, state),
                            TelecomBlocks.SIGNAL_BOX_GETTER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> TRI_STATE_SIGNAL_BOX =
            BLOCK_ENTITIES.register("tri_state_signal_box", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockTriStateSignalBox.TileEntityTriStateSignalBox(
                                    triStateSignalBoxType(), pos, state),
                            TelecomBlocks.TRI_STATE_SIGNAL_BOX.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> NSASM_BOX =
            BLOCK_ENTITIES.register("nsasm_box", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockNSASMBox.TileEntityNSASMBox(nsasmBoxType(), pos, state),
                            TelecomBlocks.NSASM_BOX.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> RS_LATCH =
            BLOCK_ENTITIES.register("rs_latch", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockRSLatch.TileEntityRSLatch(rsLatchType(), pos, state),
                            TelecomBlocks.RS_LATCH.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> TIMER =
            BLOCK_ENTITIES.register("timer", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockTimer.TileEntityTimer(timerType(), pos, state),
                            TelecomBlocks.TIMER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> DELAYER =
            BLOCK_ENTITIES.register("delayer", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockDelayer.TileEntityDelayer(delayerType(), pos, state),
                            TelecomBlocks.DELAYER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> SIGNAL_BOX_INPUT =
            BLOCK_ENTITIES.register("signal_box_input", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockRedInput.TileEntityRedInput(signalBoxInputType(), pos, state),
                            TelecomBlocks.SIGNAL_BOX_INPUT.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> SIGNAL_BOX_OUTPUT =
            BLOCK_ENTITIES.register("signal_box_output", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockRedOutput.TileEntityRedOutput(signalBoxOutputType(), pos, state),
                            TelecomBlocks.SIGNAL_BOX_OUTPUT.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> SIGNAL_BOX_RX =
            BLOCK_ENTITIES.register("signal_box_rx", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockWirelessRx.TileEntityWirelessRx(signalBoxRxType(), pos, state),
                            TelecomBlocks.SIGNAL_BOX_RX.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> SIGNAL_BOX_TX =
            BLOCK_ENTITIES.register("signal_box_tx", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockWirelessTx.TileEntityWirelessTx(signalBoxTxType(), pos, state),
                            TelecomBlocks.SIGNAL_BOX_TX.get())
                    .build(null));

    /** Shared by the two {@code nspga_t*c*i8o8r0} blocks, as in 1.12.2 ({@code TileEntityNSPGA}). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> NSPGA =
            BLOCK_ENTITIES.register("nspga", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockNSPGA.TileEntityNSPGA(nspgaType(), pos, state),
                            TelecomBlocks.NSPGA_T0C0.get(), TelecomBlocks.NSPGA_T4C4.get())
                    .build(null));

    /** Shared by the three {@code nspga_f*} blocks, as in 1.12.2 ({@code TileEntityNSPGAFlex}). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> NSPGA_FLEX =
            BLOCK_ENTITIES.register("nspga_flex", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlockNSPGAFlex.TileEntityNSPGAFlex(nspgaFlexType(), pos, state),
                            TelecomBlocks.NSPGA_F011.get(), TelecomBlocks.NSPGA_F211.get(),
                            TelecomBlocks.NSPGA_F344.get())
                    .build(null));

    /* -------- the lazy accessors the block classes and the factories use -------- */

    public static BlockEntityType<?> signalBoxType() {
        return SIGNAL_BOX.get();
    }

    public static BlockEntityType<?> signalBoxSenderType() {
        return SIGNAL_BOX_SENDER.get();
    }

    public static BlockEntityType<?> signalBoxGetterType() {
        return SIGNAL_BOX_GETTER.get();
    }

    public static BlockEntityType<?> triStateSignalBoxType() {
        return TRI_STATE_SIGNAL_BOX.get();
    }

    public static BlockEntityType<?> nsasmBoxType() {
        return NSASM_BOX.get();
    }

    public static BlockEntityType<?> rsLatchType() {
        return RS_LATCH.get();
    }

    public static BlockEntityType<?> timerType() {
        return TIMER.get();
    }

    public static BlockEntityType<?> delayerType() {
        return DELAYER.get();
    }

    public static BlockEntityType<?> signalBoxInputType() {
        return SIGNAL_BOX_INPUT.get();
    }

    public static BlockEntityType<?> signalBoxOutputType() {
        return SIGNAL_BOX_OUTPUT.get();
    }

    public static BlockEntityType<?> signalBoxRxType() {
        return SIGNAL_BOX_RX.get();
    }

    public static BlockEntityType<?> signalBoxTxType() {
        return SIGNAL_BOX_TX.get();
    }

    public static BlockEntityType<?> nspgaType() {
        return NSPGA.get();
    }

    public static BlockEntityType<?> nspgaFlexType() {
        return NSPGA_FLEX.get();
    }
}
