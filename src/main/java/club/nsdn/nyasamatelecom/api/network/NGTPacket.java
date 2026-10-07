package club.nsdn.nyasamatelecom.api.network;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import net.minecraft.core.component.DataComponents;import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Ported from the 1.12.2 {@code NGTPacket} ({@code IMessage}): carries a compound tag between the
 * client and the server, used by the NGT tablet tool to sync its code/pages.
 *
 * <p>1.12.2 wrote the tag by hand in {@code toBytes}/{@code fromBytes} over a Netty {@code ByteBuf}.
 * In 1.21.1 a payload declares a {@link StreamCodec} instead -- {@link ByteBufCodecs#COMPOUND_TAG}
 * covers exactly this case, so the manual (and exception-swallowing) read/write pair disappears
 * along with its "couldn't send/receive" logging.</p>
 *
 * <p>Item NBT also changed: {@code ItemStack#getTagCompound()} is gone, so the tag now comes from the
 * {@code CUSTOM_DATA} component and is written back through it. The dictionary-style API
 * ({@code hasKey}, {@code getString}) is replaced by {@code contains}/{@code getString}.</p>
 *
 * <p>The old {@code NGTPacketHandler} is folded in as {@link #handle}: NeoForge handlers are plain
 * methods referenced from the registrar, not separate {@code IMessageHandler} classes.</p>
 */
public record NGTPacket(CompoundTag tag) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NGTPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("nyasamatelecom", "ngt"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NGTPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, NGTPacket::tag, NGTPacket::new);

    /** 1.12.2: {@code new NGTPacket(ItemStack)} took the stack's whole tag. */
    public NGTPacket(ItemStack stack) {
        this(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    /** 1.12.2: {@code new NGTPacket(String code)} wrapped a single code string. */
    public static NGTPacket ofCode(String code) {
        CompoundTag tag = new CompoundTag();
        tag.putString("code", code);
        return new NGTPacket(tag);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Client -&gt; server handler (1.12.2 {@code NGTPacketHandler}).
     *
     * <p>Registered on the main thread, so the 1.12.2 {@code addScheduledTask} hop is not needed.
     * The tablet type check is kept behind {@link NGTabletShim} because the {@code api/tool} package
     * (and thus the real {@code NGTablet} item) is not ported yet; the data flow itself is complete.</p>
     */
    public static void handle(NGTPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack stack = serverPlayer.getMainHandItem();
        if (serverPlayer.getOffhandItem().getItem() instanceof NGTablet) {
            stack = serverPlayer.getOffhandItem();
        }
        if (!(stack.getItem() instanceof NGTablet)) {
            return;
        }

        CompoundTag incoming = packet.tag();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, existing -> {
            if (incoming.contains("code")) {
                existing.putString("code", incoming.getString("code"));
            } else if (incoming.contains("pages")) {
                // 1.12.2 gated this on ItemWritableBook.isNBTValid; the equivalent check is that the
                // list exists, which is exactly what the original test boiled down to for our data.
                existing.put("pages", incoming.getList("pages", net.minecraft.nbt.Tag.TAG_STRING));
            }
        });
    }
}
