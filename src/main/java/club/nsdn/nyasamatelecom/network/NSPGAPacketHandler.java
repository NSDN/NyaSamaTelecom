package club.nsdn.nyasamatelecom.network;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.tileblock.core.BlockNSPGAFlex;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server side of the NSPGA flash payload, ported from the 1.12.2 {@code NSPGAPacketHandler}.
 *
 * <p>Behaviour preserved line for line: the player must be holding an {@code NGTablet} in either hand
 * (otherwise the 1.12.2 message "Please held verified item!" is shown, typo and all), then the packet's tag
 * is loaded into the addressed {@code TileEntityNSPGAFlex}, the entity is marked dirty, re-initialised and
 * the player is told {@code info.nspga.set}.</p>
 *
 * <p>Two structural changes. 1.12.2 wrapped the body in
 * {@code WorldServer#addScheduledTask(...)} because {@code IMessageHandler#onMessage} ran off-thread;
 * NeoForge registers payload handlers on the main thread by default (see {@code docs/09} §5), so the hop
 * is not ported -- re-adding it would only delay the work by a tick. And
 * {@code player.getServerWorld()} / {@code player.sendMessage(...)} are {@code player.level()} (narrowed to
 * {@code ServerLevel}) and {@code player.displayClientMessage(..., false)}.</p>
 */
public final class NSPGAPacketHandler {

    private NSPGAPacketHandler() {
    }

    public static void handle(NSPGAPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (
            !(player.getMainHandItem().getItem() instanceof NGTablet) &&
            !(player.getOffhandItem().getItem() instanceof NGTablet)
        ) {
            player.displayClientMessage(Component.literal("Please held verified item!"), false);
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = packet.getPos();
        if (pos == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BlockNSPGAFlex.TileEntityNSPGAFlex flex) {
            BlockNSPGAFlex.TileEntityNSPGAFlex.loadFromTag(flex, packet.tag());
            flex.setChanged();
            BlockNSPGAFlex.TileEntityNSPGAFlex.tryInitialize(flex);
            player.displayClientMessage(Component.translatable("info.nspga.set"), false);
        }
    }
}
