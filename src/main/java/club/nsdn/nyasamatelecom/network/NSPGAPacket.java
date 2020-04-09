package club.nsdn.nyasamatelecom.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by drzzm32 on 2020.4.7.
 */
public class NSPGAPacket implements IMessage {

    static Logger logger;

    public NBTTagCompound tag;

    public NSPGAPacket() {
        tag = null;
    }

    public NSPGAPacket(BlockPos pos, int ioCount, String code) {
        this.tag = new NBTTagCompound();
        tag.setInteger("x", pos.getX());
        tag.setInteger("y", pos.getY());
        tag.setInteger("z", pos.getZ());
        tag.setInteger("io", ioCount);
        tag.setString("code", code);
    }

    public NSPGAPacket(BlockPos pos, String[] inputs, String[] outputs, String code) {
        this.tag = new NBTTagCompound();
        tag.setInteger("x", pos.getX());
        tag.setInteger("y", pos.getY());
        tag.setInteger("z", pos.getZ());
        NBTTagList list = new NBTTagList();
        for (String s : inputs)
            list.appendTag(new NBTTagString(s));
        tag.setTag("inputs", list);
        list = new NBTTagList();
        for (String s : outputs)
            list.appendTag(new NBTTagString(s));
        tag.setTag("outputs", list);
        tag.setString("code", code);
    }

    public NSPGAPacket(BlockPos pos, int ioCount, String[] inputs, String[] outputs, String code) {
        this(pos, inputs, outputs, code);
        tag.setInteger("io", ioCount);
    }

    public BlockPos getPos() {
        if (tag == null) return null;
        return new BlockPos(
                tag.getInteger("x"),
                tag.getInteger("y"),
                tag.getInteger("z")
        );
    }

    public int ioCount() {
        int io = 1;
        if (tag != null)
            io = tag.getInteger("io");
        return io;
    }

    public String[] getInputs() {
        ArrayList<String> arrayList = new ArrayList<>();
        if (tag != null) {
            NBTTagList list = tag.getTagList("inputs", 8);
            if (!list.hasNoTags())
                for (int i = 0; i < list.tagCount(); i++)
                    arrayList.add(list.getStringTagAt(i));
        }
        return arrayList.toArray(new String[]{});
    }

    public String[] getOutputs() {
        ArrayList<String> arrayList = new ArrayList<>();
        if (tag != null) {
            NBTTagList list = tag.getTagList("outputs", 8);
            if (!list.hasNoTags())
                for (int i = 0; i < list.tagCount(); i++)
                    arrayList.add(list.getStringTagAt(i));
        }
        return arrayList.toArray(new String[]{});
    }

    public int inputSize() {
        return getInputs().length;
    }

    public int outputSize() {
        return getOutputs().length;
    }

    public String getCode() {
        String code = "";
        if (tag != null)
            code = tag.getString("code");
        return code;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            (new PacketBuffer(buf)).writeCompoundTag(tag);
        } catch (Exception e) {
            if (logger != null)
                logger.error("Couldn't send NGT info", e);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            tag = (new PacketBuffer(buf)).readCompoundTag();
        } catch (Exception e) {
            if (logger != null)
                logger.error("Couldn't receive NGT info", e);
        }
    }

}
