package club.nsdn.nyasamatelecom.api.network;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by drzzm32 on 2018.12.13.
 */
public class NGTPacketHandler implements IMessageHandler<NGTPacket, IMessage> {
    @Override
    public IMessage onMessage(NGTPacket packet, MessageContext context) {
        EntityPlayerMP serverPlayer = context.getServerHandler().player;

        serverPlayer.getServerWorld().addScheduledTask(() -> {
            ItemStack stack = serverPlayer.getHeldItemMainhand();
            if (serverPlayer.getHeldItemOffhand().getItem() instanceof NGTablet)
                stack = serverPlayer.getHeldItemOffhand();
            if (stack.getItem() instanceof NGTablet) {
                if (packet.tag.hasKey("code"))
                    stack.setTagInfo("code", new NBTTagString(packet.tag.getString("code")));
                else if (ItemWritableBook.isNBTValid(packet.tag))
                    stack.setTagInfo("pages", packet.tag.getTagList("pages", 8));
            }
        });

        return null;
    }
}
