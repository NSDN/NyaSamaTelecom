package club.nsdn.nyasamatelecom.network;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

/**
 * The NSPGA flash payload, ported from the 1.12.2 {@code NSPGAPacket} ({@code IMessage}).
 *
 * <p>1.12.2 registered this one class <b>twice</b> on the same channel -- at discriminator {@code pos}
 * with {@link NSPGAPacketHandler} on {@code Side.SERVER} and at {@code pos + 1} with
 * {@link NSPGAEditorHandler} on {@code Side.CLIENT} -- so "the same message, two directions, two handlers"
 * is the original design and not an accident. NeoForge removes discriminators but keeps the idea: a
 * payload keeps one {@code Type} and the direction is decided at registration, and because
 * {@code NetworkRegistry} keys payloads by (protocol, id) rather than by flow, the two directions are
 * <b>one</b> {@code playBidirectional} registration in {@code TelecomNetwork} carrying a
 * {@code DirectionalPayloadHandler} that routes on {@code IPayloadContext#flow} to those two handlers.
 * (Two registrations of this one {@code Type} -- the literal transcription of the 1.12.2 shape -- throw
 * {@code UnsupportedOperationException: ... already registered}; see {@code TelecomNetwork}.) The 1.12.2
 * no-argument constructor existed only for the message framework to instantiate the class before
 * {@code fromBytes}; a {@link StreamCodec} replaces that, so it is gone.</p>
 *
 * <p>Everything the old hand-written {@code toBytes}/{@code fromBytes} pair did is now
 * {@link ByteBufCodecs#COMPOUND_TAG}, including its exception swallowing -- which is a real behaviour
 * change for the better: a malformed tag now fails the decode instead of silently arriving as null.
 * The tag layout and all accessors are unchanged ({@code x/y/z}, {@code io}, {@code inputs},
 * {@code outputs}, {@code code}), so this is wire-compatible with itself and readable by the same code as
 * before. The old static {@code Logger} field disappeared with {@code NetworkWrapper}: payload handlers are
 * no longer handed a logger.</p>
 */
public record NSPGAPacket(CompoundTag tag) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NSPGAPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NyaSamaTelecom.MODID, "nspga"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NSPGAPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, NSPGAPacket::tag, NSPGAPacket::new);

    /** 1.12.2: {@code NSPGAPacket(BlockPos, int ioCount, String code)} -- the "reconfigure from scratch" form. */
    public NSPGAPacket(BlockPos pos, int ioCount, String code) {
        this(build(pos, ioCount, null, null, code));
    }

    /** 1.12.2: {@code NSPGAPacket(BlockPos, String[] inputs, String[] outputs, String code)}. */
    public NSPGAPacket(BlockPos pos, String[] inputs, String[] outputs, String code) {
        this(build(pos, 0, inputs, outputs, code));
    }

    /** 1.12.2: {@code NSPGAPacket(BlockPos, int ioCount, String[] inputs, String[] outputs, String code)}. */
    public NSPGAPacket(BlockPos pos, int ioCount, String[] inputs, String[] outputs, String code) {
        this(build(pos, ioCount, inputs, outputs, code));
    }

    private static CompoundTag build(BlockPos pos, int ioCount, String[] inputs, String[] outputs, String code) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        if (inputs != null) {
            ListTag list = new ListTag();
            for (String s : inputs)
                list.add(StringTag.valueOf(s));
            tag.put("inputs", list);
        }
        if (outputs != null) {
            ListTag list = new ListTag();
            for (String s : outputs)
                list.add(StringTag.valueOf(s));
            tag.put("outputs", list);
        }
        if (ioCount > 0) {
            tag.putInt("io", ioCount);
        }
        tag.putString("code", code);
        return tag;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public BlockPos getPos() {
        if (tag == null) return null;
        return new BlockPos(
                tag.getInt("x"),
                tag.getInt("y"),
                tag.getInt("z")
        );
    }

    public int ioCount() {
        int io = 1;
        if (tag != null)
            io = tag.getInt("io");
        return io;
    }

    public String[] getInputs() {
        return readStrings("inputs");
    }

    public String[] getOutputs() {
        return readStrings("outputs");
    }

    /** 1.12.2 {@code hasNoTags()} became {@code isEmpty()} and {@code getStringTagAt(i)} became {@code getString(i)}. */
    private String[] readStrings(String key) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (tag != null) {
            ListTag list = tag.getList(key, Tag.TAG_STRING);
            if (!list.isEmpty())
                for (int i = 0; i < list.size(); i++)
                    arrayList.add(list.getString(i));
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
}
