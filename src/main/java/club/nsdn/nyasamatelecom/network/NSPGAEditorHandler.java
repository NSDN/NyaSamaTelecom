package club.nsdn.nyasamatelecom.network;

import cn.ac.nya.nspga.flex.NSPGAEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by drzzm32 on 2020.4.9.
 */
public class NSPGAEditorHandler implements IMessageHandler<NSPGAPacket, IMessage> {
    @Override
    public IMessage onMessage(NSPGAPacket packet, MessageContext context) {
        if (context.side == Side.CLIENT) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                int in = packet.inputSize(), out = packet.outputSize(), io = packet.ioCount();
                if ((in == 0 && out == 0) || (in != io || out != io)) {
                    new NSPGAEditor(io, io, packet.getCode()).setCallback((i, o, c) -> {
                        NetworkWrapper.instance.sendToServer(new NSPGAPacket(packet.getPos(), i, o, c));
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            mc.player.sendMessage(new TextComponentTranslation("info.nspga.flash"));
                        });
                    });
                } else {
                    new NSPGAEditor(packet.getInputs(), packet.getOutputs(), packet.getCode()).setCallback((i, o, c) -> {
                        NetworkWrapper.instance.sendToServer(new NSPGAPacket(packet.getPos(), i, o, c));
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            mc.player.sendMessage(new TextComponentTranslation("info.nspga.flash"));
                        });
                    });
                }

                mc.player.sendMessage(new TextComponentTranslation("info.nspga.editor"));
            });
        }

        return null;
    }
}
