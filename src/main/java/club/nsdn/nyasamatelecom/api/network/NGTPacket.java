package club.nsdn.nyasamatelecom.api.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.apache.logging.log4j.Logger;

/**
 * Created by drzzm32 on 2018.12.13.
 */
public class NGTPacket implements IMessage {

    static Logger logger;

    public NBTTagCompound tag;

    public NGTPacket() {
        tag = null;
    }

    public NGTPacket(ItemStack stack) {
        this.tag = stack.getTagCompound();
    }

    public NGTPacket(String code) {
        this.tag = new NBTTagCompound();
        tag.setString("code", code);
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
