package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import club.nsdn.nyasamatelecom.item.NyaGameMR;
import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import cn.ac.nya.nspga.INSPGA;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The programmable logic array block, ported from the 1.12.2 {@code BlockNSPGA}. Two instances exist
 * ({@code nspga_t0c0i8o8r0} and {@code nspga_t4c4i8o8r0}), each parameterised by the {@link INSPGA}
 * implementation it instantiates -- the 1.12.2 loader did the same
 * ({@code new BlockNSPGA(NSPGAT0C0.class, "BlockNSPGAT0C0", "nspga_t0c0i8o8r0")}).</p>
 *
 * <p>The wiring survives verbatim: eight input and eight output device names plus a flash word array are
 * stored on the block entity; {@code updateSignal} reads the input byte, feeds it to the device, and pushes
 * the resulting byte back out to the named devices (or to the linked target when no outputs are named).
 * {@code inputsLen == 0} still means "use my own link state as a full byte", which is the 1.12.2 rule.</p>
 *
 * <p>Mechanical mapping: {@code getBlockType()} becomes {@code getBlockState().getBlock()},
 * {@code NBTTagCompound.getInteger/setInteger} become {@code getInt/putInt}, {@code setIntArray} becomes
 * {@code putIntArray}, {@code getIntArray} becomes {@code getIntArray}, and the script engine's
 * {@code getWrapper()} override is gone from the ported {@code api.util.NSASM}.</p>
 *
 * <p><b>Transcribed hazard, not fixed:</b> {@code updateSignal} calls {@code dev.device.output(input)}
 * without re-checking that the device was created. {@code createDevice()} returns null when the reflective
 * instantiation throws, so a broken {@link INSPGA} class still NPEs the tick exactly as it did in 1.12.2.
 * This project's rule is to carry such hazards across and mark them rather than silently guard them.</p>
 *
 * <p>{@code texturePrint} was {@code @SideOnly(CLIENT)} in 1.12.2. The annotation is forbidden here and is
 * unnecessary (it only gated the field's presence in the client jar), so it is a plain field that the
 * renderer lazily fills; nothing outside the renderer reads it.</p>
 */
public class BlockNSPGA extends SignalBox {

    public final Class<? extends INSPGA> dev;
    public final String name;

    /** 1.12.2 {@code @SideOnly(Side.CLIENT) ResourceLocation texturePrint}; see the class note. */
    public ResourceLocation texturePrint;

    public static class TileEntityNSPGA extends SignalBox.TileEntitySignalBox {

        public INSPGA device = null;
        public boolean configured = false;

        public int inputsLen = 0;
        public String[] inputs = new String[0];
        public int outputsLen = 0;
        public String[] outputs = new String[0];
        public int flashLen = 0;
        public int[] flash = new int[0];

        public TileEntityNSPGA(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            setInfo(8, 0.875, 0.125, 0.875);
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            inputsLen = tagCompound.getInt("inputsLen");
            if (inputsLen > 0) {
                inputs = new String[inputsLen];
                for (int i = 0; i < inputsLen; i++) {
                    inputs[i] = tagCompound.getString("input" + i);
                }
            }
            outputsLen = tagCompound.getInt("outputsLen");
            if (outputsLen > 0) {
                outputs = new String[outputsLen];
                for (int i = 0; i < outputsLen; i++) {
                    outputs[i] = tagCompound.getString("output" + i);
                }
            }
            flashLen = tagCompound.getInt("flashLen");
            if (flashLen > 0) {
                int[] tmp = tagCompound.getIntArray("flash");
                if (tmp.length == flashLen) flash = tmp;
            }
            super.fromNBT(tagCompound);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            if (inputsLen > 0) {
                tagCompound.putInt("inputsLen", inputsLen);
                for (int i = 0; i < inputsLen; i++) {
                    tagCompound.putString("input" + i, inputs[i]);
                }
            }
            if (outputsLen > 0) {
                tagCompound.putInt("outputsLen", outputsLen);
                for (int i = 0; i < outputsLen; i++) {
                    tagCompound.putString("output" + i, outputs[i]);
                }
            }
            if (flashLen > 0) {
                tagCompound.putInt("flashLen", flashLen);
                tagCompound.putIntArray("flash", flash);
            }
            return super.toNBT(tagCompound);
        }

        @Override
        public boolean tryControlFirst(boolean state) {
            return false;
        }

        @Override
        public boolean tryControlSecond(boolean state) {
            return false;
        }

        private boolean input(String dev) {
            TelecomProcessor.DeviceInfo info;
            info = TelecomProcessor.instance().device(dev);
            boolean result;
            if (info == null) return false;
            if (!TelecomProcessor.instance().isTx(info)) {
                return false;
            }
            result = TelecomProcessor.instance().get(info);
            return result;
        }

        private void output(String dev, boolean state) {
            TelecomProcessor.DeviceInfo info;
            info = TelecomProcessor.instance().device(dev);
            if (info == null) return;
            if (!TelecomProcessor.instance().isRx(info)) {
                return;
            }
            TelecomProcessor.instance().set(info, state);
        }

        public byte input() {
            byte result = 0x00;
            if (inputsLen == 0) {
                result = isEnabled ? (byte) 0xFF : (byte) 0x00;
            } else {
                for (int i = 0; i < inputsLen; i++) {
                    result |= ((input(inputs[i]) ? 0x1 : 0x0) << i);
                }
            }
            return result;
        }

        public void output(byte data) {
            if (outputsLen == 0) {
                boolean state = (data != 0);
                if (!tryControlFirst(state)) {
                    if (!tryControlSecond(state)) {
                        if (!setTargetSender(state)) {
                            if (!setTargetGetter(state)) {
                                if (getTarget() != null) {
                                    controlTarget(state);
                                }
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < outputsLen; i++) {
                    output(outputs[i], ((data >> i) & 0x1) != 0);
                }
            }
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            if (blockEntity instanceof TileEntityNSPGA dev) {

                int meta = dev.META;
                int old = meta;
                boolean isEnabled;

                if (dev.getSender() == null) {
                    isEnabled = (meta & 0x8) != 0;
                    meta &= 0x7;
                } else {
                    isEnabled = dev.senderIsPowered();

                    if (isEnabled) meta |= 0x8;
                    else meta &= 0x7;
                }

                dev.isEnabled = isEnabled;

                if (dev.inverterEnabled)
                    dev.isEnabled = !dev.isEnabled;

                if (dev.device == null && dev.getBlockState().getBlock() instanceof BlockNSPGA)
                    dev.device = ((BlockNSPGA) dev.getBlockState().getBlock()).createDevice();

                if (!dev.configured) {
                    if (dev.flashLen > 0) {
                        dev.device.configure(dev.flash);
                    }
                }

                byte input = dev.input();
                byte output = dev.device.output(input);
                dev.output(output);

                if (old != meta) {
                    dev.META = meta;
                    dev.refresh();
                }
            }
        }
    }

    public abstract static class NSPGALoader extends NSASM {

        public NSPGALoader(String[][] code) { super(code); }

        public NSPGALoader(String code) { super(code); }

        public abstract TileEntityNSPGA getNSPGA();

        private void prt(String str) {
            Register result = new Register();
            result.type = RegType.STR; result.strPtr = 0;
            result.readOnly = true;
            result.data = str;
            funcList.get("prt").run(result, null);
        }

        private String getString(Map map, int index) {
            Register register = new Register();
            register.data = index;
            register.type = RegType.INT;
            register.readOnly = true;
            register.strPtr = 0;
            if (map.containsKey(register)) {
                register = map.get(register);
                if (register.type == RegType.STR) {
                    return register.data.toString();
                }
            }
            return "";
        }

        private int getInteger(Map map, int index) {
            Register register = new Register();
            register.data = index;
            register.type = RegType.INT;
            register.readOnly = true;
            register.strPtr = 0;
            if (map.containsKey(register)) {
                register = map.get(register);
                if (register.type == RegType.INT) {
                    return (int) register.data;
                }
            }
            return 0;
        }

        @Override
        public void loadFunc(LinkedHashMap<String, Operator> funcList) {
            funcList.replace("in", (dst, src) -> {
                if (dst == null) return Result.ERR;
                if (src != null) return Result.ERR;
                if (dst.type != RegType.MAP) return Result.ERR;

                if (dst.data instanceof Map && getNSPGA() != null) {
                    Map map = (Map) dst.data; TileEntityNSPGA dev = getNSPGA();
                    if (map.size() <= 8) {
                        dev.inputsLen = map.size();
                        dev.inputs = new String[dev.inputsLen];
                        for (int i = 0; i < dev.inputsLen; i++) {
                            dev.inputs[i] = getString(map, i);
                        }
                        return Result.OK;
                    }
                }

                return Result.ERR;
            });

            funcList.replace("out", (dst, src) -> {
                if (dst == null) return Result.ERR;
                if (src != null) return Result.ERR;
                if (dst.type != RegType.MAP) return Result.ERR;

                if (dst.data instanceof Map && getNSPGA() != null) {
                    Map map = (Map) dst.data; TileEntityNSPGA dev = getNSPGA();
                    if (map.size() <= 8) {
                        dev.outputsLen = map.size();
                        dev.outputs = new String[dev.outputsLen];
                        for (int i = 0; i < dev.outputsLen; i++) {
                            dev.outputs[i] = getString(map, i);
                        }
                        return Result.OK;
                    }
                }

                return Result.ERR;
            });

            funcList.put("flash", (dst, src) -> {
                if (dst == null) return Result.ERR;
                if (src != null) return Result.ERR;
                if (dst.type != RegType.MAP) return Result.ERR;

                if (dst.data instanceof Map && getNSPGA() != null) {
                    Map map = (Map) dst.data; TileEntityNSPGA dev = getNSPGA();
                    dev.flashLen = map.size();
                    dev.flash = new int[dev.flashLen];
                    for (int i = 0; i < dev.flashLen; i++) {
                        dev.flash[i] = getInteger(map, i) & 0xFFFF;
                    }
                    return Result.OK;
                }

                return Result.ERR;
            });

            funcList.replace("rst", (dst, src) -> {
                if (dst != null || src != null)
                    return Result.ERR;
                if (getNSPGA() != null) {
                    TileEntityNSPGA dev = getNSPGA();
                    if (dev.flashLen > 0) {
                        dev.device.configure(dev.flash);
                    }
                }
                return Result.OK;
            });
        }
    }

    public BlockNSPGA(Class<? extends INSPGA> dev, String name, String id, BlockBehaviour.Properties properties,
                      Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);

        this.dev = dev; this.name = id;
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityNSPGA(type, pos, state);
    }

    public INSPGA createDevice() {
        try {
            return dev.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityNSPGA dev) {

            if (!level.isClientSide && stack.getItem() instanceof NyaGameMR) {
                for (int i = 0; i < 9; i++) {
                    ItemStack slot = player.getInventory().getItem(i);
                    if (slot.isEmpty()) continue;
                    if (slot.getItem() == Items.AIR) continue;
                    if (slot.getItem() instanceof NGTablet) {

                        dev.inputsLen = 0; dev.inputs = new String[0];
                        dev.outputsLen = 0; dev.outputs = new String[0];
                        dev.flashLen = 0; dev.flash = new int[0];
                        dev.device = null; dev.configured = false;
                        dev.refresh();

                        Util.say(player, "info.nspga.reset");
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }

            if (!stack.isEmpty()) {
                ListTag list = Util.getTagListFromNGT(stack);
                if (list == null) {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }

                if (!level.isClientSide) {
                    String[][] code = NSASM.getCode(list);
                    new NSPGALoader(code) {
                        @Override
                        public Level getWorld() { return level; }
                        @Override
                        public Player getPlayer() { return player; }
                        @Override
                        public double getX() { return pos.getX(); }
                        @Override
                        public double getY() { return pos.getY(); }
                        @Override
                        public double getZ() { return pos.getZ(); }
                        @Override
                        public TileEntityNSPGA getNSPGA() { return dev; }
                    }.run();

                    dev.refresh();

                    Util.say(player, "info.nspga.set");
                }

                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
