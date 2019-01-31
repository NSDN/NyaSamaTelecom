package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import club.nsdn.nyasamatelecom.creativetab.CreativeTabLoader;
import club.nsdn.nyasamatelecom.util.TelecomProcessor;
import club.nsdn.nyasamatelecom.network.NetworkWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by drzzm32 on 2019.1.29.
 */
public class BlockNSASMBox extends SignalBox {

    public static class TileEntityNSASMBox extends SignalBox.TileEntitySignalBox {

        public static final int NSASM_NULL = -1, NSASM_SETUP = 0, NSASM_LOOP = 1;
        public int nsasmState = NSASM_NULL;

        public String nsasmCode = "";
        public NSASMBoxCore core = null;

        @Override
        public void fromNBT(NBTTagCompound tagCompound) {
            nsasmState = tagCompound.getInteger("nsasmState");
            nsasmCode = tagCompound.getString("nsasmCode");
            if (nsasmState == NSASM_LOOP) nsasmState = NSASM_NULL;
            super.fromNBT(tagCompound);
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tagCompound) {
            tagCompound.setInteger("nsasmState", nsasmState);
            tagCompound.setString("nsasmCode", nsasmCode);
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
        public void updateSignal(World world, BlockPos pos) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity == null) return;
            if (tileEntity instanceof TileEntityNSASMBox) {
                TileEntityNSASMBox box = (TileEntityNSASMBox) tileEntity;

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
                            public World getWorld() {
                                return world;
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

        @Override
        public SimpleNetworkWrapper getWrapper() {
            return NetworkWrapper.instance;
        }

        private TileEntityNSASMBox getBox() {
            if (getWorld() == null) return null;
            int x = MathHelper.floor(getX());
            int y = MathHelper.floor(getY());
            int z = MathHelper.floor(getZ());
            TileEntity tileEntity = getWorld().getTileEntity(new BlockPos(x, y, z));
            if (tileEntity == null) return null;
            if (!(tileEntity instanceof TileEntityNSASMBox)) return null;
            return (TileEntityNSASMBox) tileEntity;
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

        @Override
        public EntityPlayer getPlayer() {
            if (getWorld() == null) return null;
            double size = 2.0;
            List list = getWorld().getEntitiesWithinAABB(
                    EntityPlayer.class,
                    new AxisAlignedBB(
                            getX() - size + 1,
                            getY() - size + 1,
                            getZ() - size + 1,
                            getX() + size,
                            getY() + size,
                            getZ() + size
                    )
            );
            if (list.isEmpty()) return null;

            for (Object obj : list) {
                if (obj instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) obj;
                    ItemStack stack = player.getHeldItemMainhand();
                    if (!stack.isEmpty()) {
                        if (stack.getItem() instanceof NGTablet) {
                            return player;
                        }
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

    @Override
    public TileEntity createNewTileEntity() {
        return new TileEntityNSASMBox();
    }

    public BlockNSASMBox() {
        super(NyaSamaTelecom.MODID, "BlockNSASMBox", "nsasm_box");
        setCreativeTab(CreativeTabLoader.tabNyaSamaTelecom);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)  {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityNSASMBox) {
            TileEntityNSASMBox box = (TileEntityNSASMBox) tileEntity;

            ItemStack stack;
            if (!world.isRemote && player.isSneaking()) {
                for (int i = 0; i < 9; i++) {
                    stack = player.inventory.mainInventory.get(i);
                    if (stack.isEmpty()) continue;
                    if (stack.getItem() == Items.AIR) continue;
                    if (stack.getItem() instanceof NGTablet) {
                        box.nsasmState = TileEntityNSASMBox.NSASM_NULL;
                        Util.say(player, "info.nsasm.reset");
                        return true;
                    }
                }
            }

            stack = player.getHeldItem(hand);
            if (!stack.isEmpty()) {
                NBTTagList list = Util.getTagListFromNGT(stack);
                if (list == null) return false;

                if (!world.isRemote) {
                    String code = NSASM.getCodeString(list);

                    box.nsasmState = TileEntityNSASMBox.NSASM_NULL;
                    box.nsasmCode = code;

                    Util.say(player, "info.nsasm.set");
                }

                return true;
            }
        }

        return false;
    }

}
