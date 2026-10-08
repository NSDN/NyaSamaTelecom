package club.nsdn.nyasamatelecom.tileblock.core;

import club.nsdn.nyasamatelecom.api.device.TriStateSignalBox;
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
 * The three-state signal box, ported from the 1.12.2 {@code BlockTriStateSignalBox}
 * (registry id {@code tri_state_signal_box}).
 *
 * <p>Script interaction is the 1.12.2 one: {@code inv} toggles the internal inverter, {@code tri} toggles
 * the negative/positive tri-state, and both keep their original messages. The same mechanical mapping as
 * {@link BlockSignalBox} applies ({@code onBlockActivated} -&gt; {@code useItemOn}, no
 * {@code getWrapper()} override, {@code ListTag} instead of {@code NBTTagList}).</p>
 */
public class BlockTriStateSignalBox extends TriStateSignalBox {

    public static class TileEntityTriStateSignalBox extends TriStateSignalBox.TileEntityTriStateSignalBox {

        public TileEntityTriStateSignalBox(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    public BlockTriStateSignalBox(BlockBehaviour.Properties properties,
                                  Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    protected TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new TileEntityTriStateSignalBox(type, pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityTriStateSignalBox box && !stack.isEmpty()) {
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

                        funcList.put("tri", (dst, src) -> {
                            if (dst != null) return Result.ERR;
                            if (src != null) return Result.ERR;

                            box.triStateIsNeg = !box.triStateIsNeg;
                            if (box.triStateIsNeg)
                                Util.say(getPlayer(), "info.signal.box.triState.neg");
                            else
                                Util.say(getPlayer(), "info.signal.box.triState.pos");

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
