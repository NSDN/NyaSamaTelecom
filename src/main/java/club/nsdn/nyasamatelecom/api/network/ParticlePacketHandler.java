package club.nsdn.nyasamatelecom.api.network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by drzzm32 on 2018.12.13.
 */
public class ParticlePacketHandler implements IMessageHandler<ParticlePacket, IMessage> {
    @Override
    public IMessage onMessage(ParticlePacket packet, MessageContext context) {
        EnumParticleTypes type = EnumParticleTypes.getByName(packet.type);
        if (type == null) type = EnumParticleTypes.BARRIER;

        EnumParticleTypes finalType = type;
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft.getMinecraft().world.spawnParticle(
                    finalType,
                    packet.x, packet.y, packet.z,
                    packet.tX, packet.tY, packet.tZ
            );
        });

        return null;
    }
}
