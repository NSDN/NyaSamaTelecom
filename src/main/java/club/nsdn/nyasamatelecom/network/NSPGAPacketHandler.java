package club.nsdn.nyasamatelecom.network;

import club.nsdn.nyasamatelecom.tileblock.core.BlockNSPGAFlex;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by drzzm32 on 2020.4.7.
 */
public class NSPGAPacketHandler implements IMessageHandler<NSPGAPacket, IMessage> {
    @Override
    public IMessage onMessage(NSPGAPacket packet, MessageContext context) {
        EntityPlayerMP serverPlayer = context.getServerHandler().player;
        WorldServer world = serverPlayer.getServerWorld();
        world.addScheduledTask(() -> {
            BlockPos pos = packet.getPos();
            if (pos != null) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof BlockNSPGAFlex.TileEntityNSPGAFlex) {
                    BlockNSPGAFlex.TileEntityNSPGAFlex.loadFromTag(
                            (BlockNSPGAFlex.TileEntityNSPGAFlex) te, packet.tag);
                    te.markDirty();
                    BlockNSPGAFlex.TileEntityNSPGAFlex.tryInitialize((BlockNSPGAFlex.TileEntityNSPGAFlex) te);
                    serverPlayer.sendMessage(new TextComponentTranslation("info.nspga.set"));
                }
            }
        });

        return null;
    }
}
