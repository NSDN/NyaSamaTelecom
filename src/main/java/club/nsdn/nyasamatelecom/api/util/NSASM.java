package club.nsdn.nyasamatelecom.api.util;

import club.nsdn.nyasamatelecom.api.network.ParticlePacket;
import cn.ac.nya.nsasm.Util;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;

/**
 * The telecom script dialect, ported from the 1.12.2 {@code club.nsdn.nyasamatelecom.api.util.NSASM}.
 *
 * <p>It extends the {@code core} virtual machine ({@code cn.ac.nya.nsasm.NSASM}) and adds four
 * functions ({@code in}, {@code prt}, {@code nya}, {@code nyaa}) plus the {@code nil} parameter. The
 * opcode bodies are carried over unchanged -- including the deliberately odd {@code in} that only
 * checks its operands are non-null -- because they are the script language's contract.</p>
 *
 * <p>What changed around them:</p>
 * <ul>
 *   <li>{@code NBTTagList} becomes {@link ListTag} ({@code tagCount()} -&gt; {@code size()},
 *       {@code getStringTagAt(i)} -&gt; {@code getString(i)}).</li>
 *   <li>{@code TextComponentString} becomes a literal {@link Component} sent with
 *       {@code Player#displayClientMessage}.</li>
 *   <li><b>{@code getWrapper()} is gone.</b> The old abstract hook handed the script engine a
 *       {@code SimpleNetworkWrapper} so {@code nya}/{@code nyaa} could call
 *       {@code sendToDimension(this, player.dimension)}. The modern send API takes a
 *       {@link ServerLevel} instead, and {@link #getWorld()} already exposes the level, so the extra
 *       hook disappears rather than being re-invented -- one fewer abstract method for every
 *       subclass (telecom, optics, railway, electricity all have their own script subclasses).</li>
 * </ul>
 *
 * <p>Kept as-is on purpose: the {@code Double.valueOf(...)} conversions can still throw on malformed
 * script input, exactly as in 1.12.2 -- the VM reports script errors, so tightening this here would
 * change observable behaviour rather than fix a bug.</p>
 */
public abstract class NSASM extends cn.ac.nya.nsasm.NSASM {

    public static String[][] getCode(ListTag list) {
        String codeBuf = "";
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                codeBuf = codeBuf.concat(list.getString(i) + "\n");
            }
        } else {
            codeBuf = "prt \"Code is Empty!\"\n";
        }
        return Util.getSegments(codeBuf);
    }

    public static String getCodeString(ListTag list) {
        String codeBuf = "";
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                codeBuf = codeBuf.concat(list.getString(i) + "\n");
            }
        } else {
            codeBuf = "prt \"Code is Empty!\"\n";
        }
        return codeBuf;
    }

    public NSASM(String[][] code) {
        super(64, 32, 32, code);
    }

    public NSASM(String code) {
        super(64, 32, 32, Util.getSegments(code));
    }

    private void print(String value) {
        if (getPlayer() == null) {
            return;
        }
        getPlayer().displayClientMessage(Component.literal(value), false);
    }

    @Override
    protected void loadFuncList() {
        super.loadFuncList();

        funcList.replace("in", (dst, src) -> {
            if (dst == null && src == null) {
                return Result.ERR;
            }
            return Result.OK;
        });

        funcList.replace("prt", (dst, src) -> {
            if (dst == null) {
                return Result.ERR;
            }
            if (src != null) {
                if (dst.type == RegType.STR) {
                    if (dst.readOnly) {
                        return Result.ERR;
                    }
                    if (src.type == RegType.CHAR && src.data.equals('\b')) {
                        if (dst.data.toString().contains("\n")) {
                            String[] parts = dst.data.toString().split("\n");
                            String res = "";
                            for (int i = 0; i < parts.length - 1; i++) {
                                res = res.concat(parts[i]);
                                if (i < parts.length - 2) {
                                    res = res.concat("\n");
                                }
                            }
                        }
                    } else if (src.type == RegType.CODE) {
                        Register register = eval(src);
                        if (register == null) {
                            return Result.ERR;
                        }
                        dst.data = dst.data.toString().concat('\n' + register.data.toString());
                    } else if (src.type == RegType.STR) {
                        dst.data = dst.data.toString().concat('\n' + src.data.toString());
                    } else {
                        return Result.ERR;
                    }
                } else if (dst.type == RegType.CODE) {
                    if (dst.readOnly) {
                        return Result.ERR;
                    }
                    if (src.type == RegType.CHAR && src.data.equals('\b')) {
                        if (dst.data.toString().contains("\n")) {
                            String[] parts = dst.data.toString().split("\n");
                            String res = "";
                            for (int i = 0; i < parts.length - 1; i++) {
                                res = res.concat(parts[i]);
                                if (i < parts.length - 2) {
                                    res = res.concat("\n");
                                }
                            }
                        }
                    } else if (src.type == RegType.CODE) {
                        dst.data = dst.data.toString().concat('\n' + src.data.toString());
                    } else if (src.type == RegType.STR) {
                        dst.data = dst.data.toString().concat('\n' + src.data.toString());
                    } else {
                        return Result.ERR;
                    }
                } else {
                    return Result.ERR;
                }
            } else {
                if (dst.type == RegType.STR) {
                    print(((String) dst.data).substring(dst.strPtr));
                } else if (dst.type == RegType.CODE) {
                    Register register = eval(dst);
                    if (register == null) {
                        return Result.ERR;
                    }
                    print(register.data.toString());
                } else {
                    print(dst.data.toString());
                }
            }
            return Result.OK;
        });

        funcList.put("nya", (dst, src) -> {
            if (src != null || dst == null) {
                return Result.ERR;
            }
            if (dst.type != RegType.STR || dst.data.toString().isEmpty()) {
                return Result.ERR;
            }

            String type = dst.data.toString();
            double tX = regGroup[0].type != RegType.STR ? Double.valueOf(regGroup[0].data.toString()) : 0;
            double tY = regGroup[1].type != RegType.STR ? Double.valueOf(regGroup[1].data.toString()) : 0;
            double tZ = regGroup[2].type != RegType.STR ? Double.valueOf(regGroup[2].data.toString()) : 0;

            if (getPlayer() != null && getWorld() instanceof ServerLevel serverLevel) {
                new ParticlePacket(type, getX(), getY(), getZ(), tX, tY, tZ).send(serverLevel);
            }
            return Result.OK;
        });

        funcList.put("nyaa", (dst, src) -> {
            if (src != null || dst == null) {
                return Result.ERR;
            }
            if (dst.type != RegType.STR || dst.data.toString().isEmpty()) {
                return Result.ERR;
            }

            String type = dst.data.toString();
            double x = regGroup[0].type != RegType.STR ? Double.valueOf(regGroup[0].data.toString()) : 0;
            double y = regGroup[1].type != RegType.STR ? Double.valueOf(regGroup[1].data.toString()) : 0;
            double z = regGroup[2].type != RegType.STR ? Double.valueOf(regGroup[2].data.toString()) : 0;
            double tX = regGroup[3].type != RegType.STR ? Double.valueOf(regGroup[3].data.toString()) : 0;
            double tY = regGroup[4].type != RegType.STR ? Double.valueOf(regGroup[4].data.toString()) : 0;
            double tZ = regGroup[5].type != RegType.STR ? Double.valueOf(regGroup[5].data.toString()) : 0;

            if (getPlayer() != null && getWorld() instanceof ServerLevel serverLevel) {
                new ParticlePacket(type, x, y, z, tX, tY, tZ).send(serverLevel);
            }
            return Result.OK;
        });

        loadFunc(funcList);
    }

    @Override
    protected void loadParamList() {
        super.loadParamList();

        paramList.put("nil", (reg) -> {
            if (reg == null) {
                Register res = new Register();
                res.type = RegType.STR;
                res.readOnly = true;
                res.data = "";
                return res;
            }
            return reg;
        });
    }

    public abstract Level getWorld();

    public abstract double getX();

    public abstract double getY();

    public abstract double getZ();

    public abstract Player getPlayer();

    public abstract void loadFunc(LinkedHashMap<String, Operator> funcList);
}
