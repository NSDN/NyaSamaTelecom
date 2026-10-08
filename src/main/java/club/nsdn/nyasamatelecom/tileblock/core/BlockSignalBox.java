package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The plain signal box, ported from the 1.12.2 {@code BlockSignalBox} (registry id {@code signal_box}).
 *
 * <p>Its tile entity only neutralises both control hooks, and the block adds the NGT-script
 * interaction that toggles the inverter. Everything else is inherited from
 * {@code api.device.SignalBox}.</p>
 *
 * <p>Mechanical mapping: {@code onBlockActivated(world, pos, state, player, hand, facing, ...)} becomes
 * {@link #useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
 * (verified present on {@code BlockBehaviour} in NeoForge 21.1.256), {@code world.getTileEntity(pos)}
 * becomes {@code level.getBlockEntity(pos)}, {@code EntityPlayer} becomes {@link Player},
 * {@code world.isRemote} becomes {@code level.isClientSide}, and {@code NBTTagList} becomes
 * {@link ListTag}. The script engine's {@code getWrapper()} hook is gone from the ported
 * {@code api.util.NSASM}, so the anonymous script below no longer overrides it -- the {@code nya}/{@code nyaa}
 * opcodes send through {@code getWorld()} instead. The 1.12.2 return values map as
 * {@code true} -&gt; {@link ItemInteractionResult#SUCCESS} and {@code false} -&gt;
 * {@link ItemInteractionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION}.</p>
 */
public class BlockSignalBox extends SignalBox {

    public static class TileEntitySignalBox
            extends club.nsdn.nyasamatelecom.api.device.SignalBox.TileEntitySignalBox {

        public TileEntitySignalBox(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public boolean tryControlFirst(boolean state) {
            return false;
        }

        @Override
        public boolean tryControlSecond(boolean state) {
            return false;
        }
    }

    public BlockSignalBox(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntitySignalBox(type, pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntitySignalBox box && !stack.isEmpty()) {
            ListTag list = Util.getTagListFromNGT(stack);
            if (list == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (!level.isClientSide) {
                String[][] code = NSASM.getCode(list);
                new NSASM(code) {
                    @Override
                    public Level getWorld() {
                        return level;
                    }

                    @Override
                    public double getX() {
                        return pos.getX();
                    }

                    @Override
                    public double getY() {
                        return pos.getY();
                    }

                    @Override
                    public double getZ() {
                        return pos.getZ();
                    }

                    @Override
                    public Player getPlayer() {
                        return player;
                    }

                    @Override
                    public void loadFunc(LinkedHashMap<String, Operator> funcList) {
                        funcList.put("inv", (dst, src) -> {
                            if (dst != null) return Result.ERR;
                            if (src != null) return Result.ERR;

                            box.inverterEnabled = !box.inverterEnabled;
                            if (box.inverterEnabled)
                                Util.say(getPlayer(), "info.signal.box.inverter.on");
                            else
                                Util.say(getPlayer(), "info.signal.box.inverter.off");

                            return Result.OK;
                        });
                    }
                }.run();
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
