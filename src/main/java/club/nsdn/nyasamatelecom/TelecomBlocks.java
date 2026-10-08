package club.nsdn.nyasamatelecom;

import club.nsdn.nyasamatelecom.block.BlockLogo;
import club.nsdn.nyasamatelecom.block.BlockNSDNLogo;
import club.nsdn.nyasamatelecom.block.BlockSign;
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
import cn.ac.nya.nspga.NSPGAT0C0;
import cn.ac.nya.nspga.NSPGAT4C4;
import cn.ac.nya.nspga.flex.NSPGAF011NHV1;
import cn.ac.nya.nspga.flex.NSPGAF211NHV1;
import cn.ac.nya.nspga.flex.NSPGAF344NHV1;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block registration for {@code nyasamatelecom}.
 *
 * <p>This class <b>replaces</b> the 1.12.2 {@code block.BlockLoader}. 1.12.2 built a
 * {@code LinkedList<Block>}, filled it in a constructor and pushed it into the registry from a
 * {@code RegistryEvent.Register<Block>} handler, then made a matching {@code ItemBlock} for every block
 * from a second handler and an item-icon {@code ModelRegistryEvent} handler. None of those four things
 * exist in 1.21.1 (deferred registers replace the registry events, and item icons are plain JSON), so
 * what is left is the table walk below -- the same shape {@code nyasamaoptics} and
 * {@code nyasamabuilding} use.</p>
 *
 * <p>The 1.12.2 registration order is preserved and the registry ids are byte-identical to the old
 * {@code setRegistryName(...)} arguments, because the shipped asset file names (blockstates, item
 * models, textures, lang keys) are keyed off those ids.</p>
 *
 * <h2>Block types ({@code BLOCK_TYPES})</h2>
 * <p>1.20.5+ requires every block class to answer {@code codec()} and be registered under
 * {@code BuiltInRegistries.BLOCK_TYPE}. <b>This module registers none.</b> Every telecom block either
 * inherits {@code Block.CODEC} (via the ported {@code api.device.DeviceBase} tree) or inherits a vanilla
 * codec ({@code TransparentBlock}), so {@code codec()} is never overridden here and adding entries would
 * be dead weight -- and would defeat the point of registering a type at all (each entry must map a
 * distinct codec to an id). See the module report for the explicit "only classes that override
 * {@code codec()} get a BLOCK_TYPES entry" rule.</p>
 *
 * <h2>Properties come from the caller now</h2>
 * <p>1.12.2's {@code DeviceBase(Material.ROCK, name)} fixed hardness 2.0, light level 0, light opacity 0,
 * {@code SoundType.STONE} and resistance 10.0, plus {@code isOpaqueCube()}/{@code isFullCube()} false.
 * In 1.21.1 those live in {@link BlockBehaviour.Properties}, so {@link #deviceProps()} reproduces exactly
 * that set (the two "not a full/opaque cube" overrides collapse into {@code noOcclusion()}). The three
 * glass panels reproduce 1.12.2's {@code BlockGlass} setup: {@code Material.GLASS}, hardness 2.0,
 * resistance 10.0, {@code SoundType.GLASS} and {@code setLightLevel(1)} -- which is a light <i>level</i>
 * of 15, since 1.12.2 scaled the float by 15.</p>
 */
public final class TelecomBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NyaSamaTelecom.MODID);

    /** The block items of {@link #BLOCKS}; the four real items live in {@link TelecomItems}. */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NyaSamaTelecom.MODID);

    private TelecomBlocks() {
    }

    /* -------- 1.12.2 BlockLoader: the three glass panels -------- */

    public static final DeferredBlock<BlockLogo> NST_LOGO =
            BLOCKS.register("nst_logo", () -> new BlockLogo(glassProps()));

    public static final DeferredBlock<BlockSign> NST_SIGN =
            BLOCKS.register("nst_sign", () -> new BlockSign(glassProps()));

    public static final DeferredBlock<BlockNSDNLogo> NST_NSDN =
            BLOCKS.register("nst_nsdn", () -> new BlockNSDNLogo(glassProps()));

    /* -------- 1.12.2 BlockLoader: the devices -------- */

    public static final DeferredBlock<BlockNSPGA> NSPGA_T0C0 = BLOCKS.register("nspga_t0c0i8o8r0",
            () -> new BlockNSPGA(NSPGAT0C0.class, "BlockNSPGAT0C0", "nspga_t0c0i8o8r0",
                    deviceProps(), TelecomBlockEntities::nspgaType));

    public static final DeferredBlock<BlockNSPGA> NSPGA_T4C4 = BLOCKS.register("nspga_t4c4i8o8r0",
            () -> new BlockNSPGA(NSPGAT4C4.class, "BlockNSPGAT4C4", "nspga_t4c4i8o8r0",
                    deviceProps(), TelecomBlockEntities::nspgaType));

    public static final DeferredBlock<BlockNSPGAFlex> NSPGA_F011 = BLOCKS.register("nspga_f011nhv1",
            () -> new BlockNSPGAFlex(NSPGAF011NHV1.class, "BlockNSPGAF011", "nspga_f011nhv1", 8,
                    deviceProps(), TelecomBlockEntities::nspgaFlexType));

    public static final DeferredBlock<BlockNSPGAFlex> NSPGA_F211 = BLOCKS.register("nspga_f211nhv1",
            () -> new BlockNSPGAFlex(NSPGAF211NHV1.class, "BlockNSPGAF211", "nspga_f211nhv1", 8,
                    deviceProps(), TelecomBlockEntities::nspgaFlexType));

    public static final DeferredBlock<BlockNSPGAFlex> NSPGA_F344 = BLOCKS.register("nspga_f344nhv1",
            () -> new BlockNSPGAFlex(NSPGAF344NHV1.class, "BlockNSPGAF344", "nspga_f344nhv1", 32,
                    deviceProps(), TelecomBlockEntities::nspgaFlexType));

    public static final DeferredBlock<BlockNSASMBox> NSASM_BOX = BLOCKS.register("nsasm_box",
            () -> new BlockNSASMBox(deviceProps(), TelecomBlockEntities::nsasmBoxType));

    public static final DeferredBlock<BlockSignalBox> SIGNAL_BOX = BLOCKS.register("signal_box",
            () -> new BlockSignalBox(deviceProps(), TelecomBlockEntities::signalBoxType));

    public static final DeferredBlock<BlockSignalBoxSender> SIGNAL_BOX_SENDER = BLOCKS.register("signal_box_sender",
            () -> new BlockSignalBoxSender(deviceProps(), TelecomBlockEntities::signalBoxSenderType));

    public static final DeferredBlock<BlockSignalBoxGetter> SIGNAL_BOX_GETTER = BLOCKS.register("signal_box_getter",
            () -> new BlockSignalBoxGetter(deviceProps(), TelecomBlockEntities::signalBoxGetterType));

    public static final DeferredBlock<BlockTriStateSignalBox> TRI_STATE_SIGNAL_BOX =
            BLOCKS.register("tri_state_signal_box",
                    () -> new BlockTriStateSignalBox(deviceProps(), TelecomBlockEntities::triStateSignalBoxType));

    public static final DeferredBlock<BlockRedInput> SIGNAL_BOX_INPUT = BLOCKS.register("signal_box_input",
            () -> new BlockRedInput(deviceProps(), TelecomBlockEntities::signalBoxInputType));

    public static final DeferredBlock<BlockRedOutput> SIGNAL_BOX_OUTPUT = BLOCKS.register("signal_box_output",
            () -> new BlockRedOutput(deviceProps(), TelecomBlockEntities::signalBoxOutputType));

    public static final DeferredBlock<BlockWirelessRx> SIGNAL_BOX_RX = BLOCKS.register("signal_box_rx",
            () -> new BlockWirelessRx(deviceProps(), TelecomBlockEntities::signalBoxRxType));

    public static final DeferredBlock<BlockWirelessTx> SIGNAL_BOX_TX = BLOCKS.register("signal_box_tx",
            () -> new BlockWirelessTx(deviceProps(), TelecomBlockEntities::signalBoxTxType));

    public static final DeferredBlock<BlockRSLatch> RS_LATCH = BLOCKS.register("rs_latch",
            () -> new BlockRSLatch(deviceProps(), TelecomBlockEntities::rsLatchType));

    public static final DeferredBlock<BlockTimer> TIMER = BLOCKS.register("timer",
            () -> new BlockTimer(deviceProps(), TelecomBlockEntities::timerType));

    public static final DeferredBlock<BlockDelayer> DELAYER = BLOCKS.register("delayer",
            () -> new BlockDelayer(deviceProps(), TelecomBlockEntities::delayerType));

    /**
     * Registers the blocks, their block items and the creative tab entry point.
     *
     * <p>Called from {@link NyaSamaTelecom}'s constructor. Nothing else calls this: a registration layer
     * that is never invoked compiles perfectly and registers nothing, which this project has already paid
     * for once (see {@code docs/13-WAVE-BRIEFS.md} §15, "the optics registration layer was never called").</p>
     */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);

        ITEMS.registerSimpleBlockItem(NST_LOGO);
        ITEMS.registerSimpleBlockItem(NST_SIGN);
        ITEMS.registerSimpleBlockItem(NST_NSDN);

        ITEMS.registerSimpleBlockItem(NSPGA_T0C0);
        ITEMS.registerSimpleBlockItem(NSPGA_T4C4);
        ITEMS.registerSimpleBlockItem(NSPGA_F011);
        ITEMS.registerSimpleBlockItem(NSPGA_F211);
        ITEMS.registerSimpleBlockItem(NSPGA_F344);

        ITEMS.registerSimpleBlockItem(NSASM_BOX);
        ITEMS.registerSimpleBlockItem(SIGNAL_BOX);
        ITEMS.registerSimpleBlockItem(SIGNAL_BOX_SENDER);
        ITEMS.registerSimpleBlockItem(SIGNAL_BOX_GETTER);
        ITEMS.registerSimpleBlockItem(TRI_STATE_SIGNAL_BOX);

        ITEMS.registerSimpleBlockItem(SIGNAL_BOX_INPUT);
        ITEMS.registerSimpleBlockItem(SIGNAL_BOX_OUTPUT);

        ITEMS.registerSimpleBlockItem(SIGNAL_BOX_RX);
        ITEMS.registerSimpleBlockItem(SIGNAL_BOX_TX);

        ITEMS.registerSimpleBlockItem(RS_LATCH);
        ITEMS.registerSimpleBlockItem(TIMER);
        ITEMS.registerSimpleBlockItem(DELAYER);
    }

    /** 1.12.2 {@code BlockGlass} setup; note {@code setLightLevel(1)} means level 15, not 1. */
    private static BlockBehaviour.Properties glassProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .sound(SoundType.GLASS)
                .strength(2.0F, 10.0F)
                .lightLevel(state -> 15)
                .noOcclusion();
    }

    /**
     * 1.12.2 {@code DeviceBase(Material.ROCK, name)}: hardness 2.0, resistance 10.0, light level 0,
     * {@code SoundType.STONE}, and the {@code isOpaqueCube()/isFullCube() == false} pair, which is
     * {@code noOcclusion()} in 1.21.1. A fresh instance per block on purpose -- {@code Properties} is
     * mutable and sharing one instance would leak later tweaks into every sibling.
     */
    private static BlockBehaviour.Properties deviceProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(2.0F, 10.0F)
                .noOcclusion();
    }
}
