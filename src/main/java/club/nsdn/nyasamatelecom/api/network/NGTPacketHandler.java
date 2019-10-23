package club.nsdn.nyasamatelecom.api.network;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by drzzm32 on 2018.12.13.
 */
public class NGTPacketHandler implements IMessageHandler<NGTPacket, IMessage> {
    @Override
    public IMessage onMessage(NGTPacket packet, MessageContext context) {
        EntityPlayerMP serverPlayer = context.getServerHandler().player;

        ItemStack stack = serverPlayer.getHeldItemMainhand();
        if (stack.getItem() instanceof NGTablet && packet.tag != null)
            stack.setTagCompound(packet.tag.copy());

        return null;
    }
}
