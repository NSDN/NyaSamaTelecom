package club.nsdn.nyasamatelecom.api.device;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityMultiSender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * "Sender" signal box, ported from the 1.12.2 {@code SignalBoxSender}: a manually toggled box whose
 * enabled state gates on its transceiver link ({@code isEnabled && transceiverIsPowered()}).
 *
 * <p>Its click-to-toggle interaction is the one place in this family with real input handling, so the
 * mapping is worth stating: 1.12.2 {@code onBlockActivated} becomes
 * {@link #useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)} (verified present in
 * NeoForge 21.1.256), the sound category {@code SoundCategory.BLOCKS} becomes
 * {@link SoundSource#BLOCKS}, and the 1.12.2 sound names lose their {@code BLOCK_} prefix
 * ({@code SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON} -&gt; {@link SoundEvents#STONE_BUTTON_CLICK_ON}).</p>
 */
public class SignalBoxSender extends DeviceBase {

    public static class TileEntitySignalBoxSender extends TileEntityMultiSender {

        public boolean isEnabled;
        public boolean prevIsEnabled;

        public TileEntitySignalBoxSender(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            setInfo(8, 0.75, 0.5, 0.75);
        }

        @Override
        public void updateSignal(Level level, BlockPos pos) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TileEntitySignalBoxSender signalBox)) {
                return;
            }

            int meta = signalBox.META;
            int old = meta;
            boolean isEnabled = signalBox.isEnabled;

            if (signalBox.getTransceiver() != null) {
                isEnabled = isEnabled && signalBox.transceiverIsPowered();
            }

            if (isEnabled) {
                meta |= 0x8;
            } else {
                meta &= 0x7;
            }

            if (old != meta || signalBox.isEnabled != signalBox.prevIsEnabled) {
                signalBox.prevIsEnabled = signalBox.isEnabled;
                signalBox.META = meta;
                signalBox.refresh();
            }
        }

        @Override
        public void fromNBT(CompoundTag tagCompound) {
            isEnabled = tagCompound.getBoolean("isEnabled");
            super.fromNBT(tagCompound);
        }

        @Override
        public CompoundTag toNBT(CompoundTag tagCompound) {
            tagCompound.putBoolean("isEnabled", isEnabled);
            return super.toNBT(tagCompound);
        }
    }

    public SignalBoxSender(BlockBehaviour.Properties properties,
                           Supplier<BlockEntityType<? extends TileEntityBase>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntitySignalBoxSender(type, pos, state);
    }

    /** Floor/wall only, as in 1.12.2. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        int val = Mth.floor((double) (placer.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
        if (placer.getXRot() > 22.5F) {
            setDeviceMeta(level, pos, val);
        } else {
            setDeviceMeta(level, pos, val + 4);
        }
    }

    protected void playClickSound(Level level, BlockPos pos, boolean state) {
        level.playSound(null, pos,
                state ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF,
                SoundSource.BLOCKS, 0.3F, state ? 0.6F : 0.5F);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntitySignalBoxSender sender) {
            if (!level.isClientSide) {
                sender.prevIsEnabled = sender.isEnabled;
                sender.isEnabled = !sender.isEnabled;
                playClickSound(level, pos, sender.isEnabled);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return false;
    }
}
