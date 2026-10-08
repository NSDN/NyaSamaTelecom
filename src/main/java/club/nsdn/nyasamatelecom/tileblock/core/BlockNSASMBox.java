package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * The programmable executor box, ported from the 1.12.2 {@code BlockNSASMBox} (registry id
 * {@code nsasm_box}).
 *
 * <p>The three-phase script lifecycle is preserved exactly: {@code NSASM_NULL} builds the VM from the
 * stored code, {@code NSASM_SETUP} calls {@code <setup>} once, {@code NSASM_LOOP} calls {@code <loop>} on
 * every tick after that. {@code fromNBT} still demotes a saved {@code NSASM_LOOP} back to
 * {@code NSASM_NULL}, which is what restarts the script after a reload.</p>
 *
 * <p>Mechanical changes only: {@code getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB)} becomes
 * {@code getEntitiesOfClass(Player.class, AABB)}, {@code MathHelper.floor} becomes {@link Mth#floor},
 * {@code getHeldItemMainhand} becomes {@code getMainHandItem}, {@code player.isSneaking()} becomes
 * {@code isShiftKeyDown()}, {@code player.inventory.mainInventory.get(i)} becomes
 * {@code player.getInventory().getItem(i)}, and the script engine's {@code getWrapper()} override is gone
 * from the ported {@code api.util.NSASM}. The device lookups inside {@code in}/{@code out} keep their
 * exact 1.12.2 messages ("[NST] Device Not Found: ..." / "[NST] Device Type Error: ...") and still return
 * {@code Result.OK} after printing, so a missing device does not abort the script.</p>
 */
public class BlockNSASMBox extends SignalBox {

    public static class TileEntityNSASMBox extends SignalBox.TileEntitySignalBox {

        public static final int NSASM_NULL = -1, NSASM_SETUP = 0, NSASM_LOOP = 1;
        public int nsasmState = NSASM_NULL;

        public String nsasmCode = "";
        public NSASMBoxCore core = null;

        public TileEntityNSASMBox(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            nsasmState = tagCompound.getInt("nsasmState");
            nsasmCode = tagCompound.getString("nsasmCode");
            if (nsasmState == NSASM_LOOP) nsasmState = NSASM_NULL;
            super.fromNBT(tagCompound);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putInt("nsasmState", nsasmState);
            tagCompound.putString("nsasmCode", nsasmCode);
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

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            if (blockEntity instanceof TileEntityNSASMBox box) {

                int meta = box.META;
                int old = meta;
                boolean isEnabled;

                if (box.getSender() == null) {
                    isEnabled = (meta & 0x8) != 0;
                    meta &= 0x7;
                } else {
                    isEnabled = box.senderIsPowered();

                    if (isEnabled) meta |= 0x8;
                    else meta &= 0x7;
                }

                box.isEnabled = isEnabled;

                switch (box.nsasmState) {
                    case TileEntityNSASMBox.NSASM_NULL:
                        if (box.nsasmCode.isEmpty()) break;
                        box.core = new NSASMBoxCore(box.nsasmCode) {
                            @Override
                            public Level getWorld() {
                                return level;
                            }

                            @Override
                            public double getX() {
                                return pos.getX();
                            }

                            @Override
                            public double getY() {
                                return pos.getY();
                            }

                            @Override
                            public double getZ() {
                                return pos.getZ();
                            }
                        };
                        box.nsasmState = TileEntityNSASMBox.NSASM_SETUP;
                        break;
                    case TileEntityNSASMBox.NSASM_SETUP:
                        if (box.core == null) break;
                        box.core.call("<setup>");
                        box.nsasmState = TileEntityNSASMBox.NSASM_LOOP;
                        break;
                    case TileEntityNSASMBox.NSASM_LOOP:
                        if (box.core == null) break;
                        box.core.call("<loop>");
                        break;
                }

                if (old != meta) {
                    box.META = meta;
                    box.refresh();
                }
            }
        }
    }

    public abstract static class NSASMBoxCore extends NSASM {

        public NSASMBoxCore(String[][] code) { super(code); }

        public NSASMBoxCore(String code) { super(code); }

        private TileEntityNSASMBox getBox() {
            if (getWorld() == null) return null;
            int x = Mth.floor(getX());
            int y = Mth.floor(getY());
            int z = Mth.floor(getZ());
            BlockEntity blockEntity = getWorld().getBlockEntity(new BlockPos(x, y, z));
            if (blockEntity == null) return null;
            if (!(blockEntity instanceof TileEntityNSASMBox)) return null;
            return (TileEntityNSASMBox) blockEntity;
        }

        private void doOutput(TileEntityNSASMBox box, boolean isEnabled) {
            if (!box.tryControlFirst(isEnabled)) {
                if (!box.tryControlSecond(isEnabled)) {
                    if (!box.setTargetSender(isEnabled)) {
                        if (!box.setTargetGetter(isEnabled)) {
                            if (box.getTarget() != null) {
                                box.controlTarget(isEnabled);
                            }
                        }
                    }
                }
            }
        }

        private void prt(String str) {
            Register result = new Register();
            result.type = RegType.STR; result.strPtr = 0;
            result.readOnly = true;
            result.data = str;
            funcList.get("prt").run(result, null);
        }

        /**
         * 1.12.2 looked for a nearby player holding an {@code NGTablet} and used it as the script's player
         * (that is how {@code prt} reaches a human without an inventory event). The search radius and the
         * "must hold a tablet in the main hand" rule are unchanged; only the query itself is modern.
         */
        @Override
        public Player getPlayer() {
            if (getWorld() == null) return null;
            double size = 2.0;
            List<Player> list = getWorld().getEntitiesOfClass(
                    Player.class,
                    new AABB(
                            getX() - size + 1,
                            getY() - size + 1,
                            getZ() - size + 1,
                            getX() + size,
                            getY() + size,
                            getZ() + size
                    )
            );
            if (list.isEmpty()) return null;

            for (Player player : list) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof NGTablet) {
                        return player;
                    }
                }
            }
            return null;
        }

        @Override
        public void loadFunc(LinkedHashMap<String, Operator> funcList) {
            funcList.replace("in", (dst, src) -> {
                if (dst == null) return Result.ERR;
                if (dst.readOnly) return Result.ERR;

                if (src == null) {
                    if (getBox() == null) return Result.ERR;

                    if (dst.type == RegType.INT) {
                        dst.data = getBox().isEnabled ? 1 : 0;
                        return Result.OK;
                    }
                    if (dst.type == RegType.FLOAT) {
                        dst.data = getBox().isEnabled ? 1.0F: 0.0F;
                        return Result.OK;
                    }
                    if (dst.type == RegType.CHAR) {
                        dst.data = getBox().isEnabled ? 1 : 0;
                        return Result.OK;
                    }
                } else {
                    if (src.type != RegType.STR) return Result.ERR;

                    TelecomProcessor.DeviceInfo info;
                    info = TelecomProcessor.instance().device((String) src.data);
                    if (info == null) {
                        prt("[NST] Device Not Found: " + src.data);
                        return Result.OK;
                    }
                    if (!TelecomProcessor.instance().isTx(info)) {
                        prt("[NST] Device Type Error: " + src.data);
                        return Result.OK;
                    }

                    boolean state = TelecomProcessor.instance().get(info);
                    if (dst.type == RegType.INT) {
                        dst.data = state ? 1 : 0;
                        return Result.OK;
                    }
                    if (dst.type == RegType.FLOAT) {
                        dst.data = state ? 1.0F: 0.0F;
                        return Result.OK;
                    }
                    if (dst.type == RegType.CHAR) {
                        dst.data = state ? 1 : 0;
                        return Result.OK;
                    }
                }

                return Result.ERR;
            });

            funcList.replace("out", (dst, src) -> {
                if (dst == null) return Result.ERR;

                if (src == null) {
                    if (getBox() == null) return Result.ERR;

                    if (dst.type == RegType.INT) {
                        doOutput(getBox(), ((int) dst.data) > 0);
                        return Result.OK;
                    }
                    if (dst.type == RegType.FLOAT) {
                        doOutput(getBox(), ((float) dst.data) > 0);
                        return Result.OK;
                    }
                    if (dst.type == RegType.CHAR) {
                        doOutput(getBox(), ((char) dst.data) > 0);
                        return Result.OK;
                    }
                } else {
                    if (dst.type != RegType.STR) return Result.ERR;

                    TelecomProcessor.DeviceInfo info;
                    info = TelecomProcessor.instance().device((String) dst.data);
                    if (info == null) {
                        prt("[NST] Device Not Found: " + dst.data);
                        return Result.OK;
                    }
                    if (!TelecomProcessor.instance().isRx(info)) {
                        prt("[NST] Device Type Error: " + dst.data);
                        return Result.OK;
                    }

                    if (src.type == RegType.INT) {
                        TelecomProcessor.instance().set(info, ((int) src.data) > 0);
                        return Result.OK;
                    }
                    if (src.type == RegType.FLOAT) {
                        TelecomProcessor.instance().set(info, ((float) src.data) > 0);
                        return Result.OK;
                    }
                    if (src.type == RegType.CHAR) {
                        TelecomProcessor.instance().set(info, ((char) src.data) > 0);
                        return Result.OK;
                    }
                }

                return Result.OK;
            });

            funcList.replace("rst", (dst, src) -> {
                if (dst == null && src == null) {
                    if (getBox() == null) return Result.ERR;
                    getBox().nsasmState = TileEntityNSASMBox.NSASM_NULL;

                    return Result.OK;
                }
                return Result.ERR;
            });
        }
    }

    public BlockNSASMBox(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityNSASMBox(type, pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityNSASMBox box) {

            if (!level.isClientSide && player.isShiftKeyDown()) {
                for (int i = 0; i < 9; i++) {
                    ItemStack slot = player.getInventory().getItem(i);
                    if (slot.isEmpty()) continue;
                    if (slot.getItem() == Items.AIR) continue;
                    if (slot.getItem() instanceof NGTablet) {
                        box.nsasmState = TileEntityNSASMBox.NSASM_NULL;
                        Util.say(player, "info.nsasm.reset");
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
                    String code = NSASM.getCodeString(list);

                    box.nsasmState = TileEntityNSASMBox.NSASM_NULL;
                    box.nsasmCode = code;

                    Util.say(player, "info.nsasm.set");
                }

                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
