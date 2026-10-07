package club.nsdn.nyasamatelecom.api.tool;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityActuator;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityMultiSender;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityPassiveReceiver;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityTransceiver;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Signal connector tool, ported from the 1.12.2 {@code Connector}.
 *
 * <p>This is the wiring tool: shift-click starts a link at a transceiver, a plain click then connects
 * senders to receivers (or actuators to their targets). The three static maps
 * ({@code tmpRails} / {@code senderRails} / {@code receiverRails}) keyed by player UUID <b>are the
 * tool's state machine</b> -- first click parks a device, second click commits or aborts. The whole
 * decision tree is therefore reproduced with the original nesting intact, including the original
 * comments, so a diff against 1.12.2 still reads as the same logic.</p>
 *
 * <p>Only the API calls differ: {@code TileEntity} -&gt; {@link BlockEntity},
 * {@code world.getTileEntity} -&gt; {@code level.getBlockEntity},
 * {@code player.getUniqueID()} -&gt; {@code player.getUUID()},
 * {@code player.isSneaking()} -&gt; {@code player.isShiftKeyDown()},
 * {@code onItemUseFirst(player, world, pos, ...)} -&gt; {@code onItemUseFirst(ItemStack, UseOnContext)},
 * {@code onItemRightClick} -&gt; {@code use}, and {@code EnumActionResult} -&gt;
 * {@link InteractionResult}.</p>
 *
 * <p>Dropped: {@code Block block = world.getBlockState(pos).getBlock();} at the top of the old method
 * was an unused local, so it is not carried over (noted rather than silently removed).</p>
 */
public class Connector extends ToolBase {

    public static LinkedHashMap<UUID, TileEntityTransceiver> tmpRails;

    public static LinkedHashMap<UUID, TileEntityTransceiver> senderRails;
    public static LinkedHashMap<UUID, TileEntityReceiver> receiverRails;

    public Connector(Item.Properties properties, Tier tier) {
        super(properties, tier);

        tmpRails = new LinkedHashMap<>();

        senderRails = new LinkedHashMap<>();
        receiverRails = new LinkedHashMap<>();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    /** Extension hook kept from 1.12.2: subclasses may claim devices the connector does not know. */
    public boolean onConnectorUseLast(Player player, UUID uuid, BlockEntity blockEntity) {
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        net.minecraft.core.BlockPos pos = context.getClickedPos();

        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                UUID uuid = player.getUUID();
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TileEntityTransceiver railSender) {
                    if (tmpRails.containsKey(uuid)) {
                        tmpRails.remove(uuid);
                        Util.say(player, "info.signal.abort");
                    } else {
                        tmpRails.put(uuid, railSender);
                        Util.say(player, "info.signal.begin");
                    }
                    return InteractionResult.SUCCESS;
                } else if (blockEntity instanceof TileEntityReceiver railReceiver) {
                    if (tmpRails.containsKey(uuid)) {
                        if (railReceiver.getSender() != tmpRails.get(uuid)) {
                            if (tmpRails.get(uuid).getTransceiver() == null) {
                                Util.say(player, "info.signal.error");
                            } else {
                                railReceiver.setSender(tmpRails.get(uuid));
                                Util.say(player, "info.signal.connected");
                                updateTileEntity(tmpRails.get(uuid));
                                updateTileEntity(railReceiver);
                            }
                            tmpRails.remove(uuid);
                        } else {
                            if (railReceiver.getSender() == tmpRails.get(uuid)) {
                                railReceiver.setSender(null);
                                Util.say(player, "info.signal.disconnected");
                                updateTileEntity(tmpRails.get(uuid));
                                updateTileEntity(railReceiver);
                                tmpRails.remove(uuid);
                            }
                        }
                    } else {
                        Util.say(player, "info.signal.error");
                    }
                    return InteractionResult.SUCCESS;
                }
            } else {
                UUID uuid = player.getUUID();
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TileEntityMultiSender sender) {
                    if (senderRails.containsKey(uuid)) {
                        if (senderRails.get(uuid) instanceof TileEntityMultiSender) {
                            if (senderRails.get(uuid) == sender) {
                                Util.say(player, "info.signal.abort");
                            } else {
                                Util.say(player, "info.signal.error");
                            }
                        } else {
                            if (sender.getTransceiver() == senderRails.get(uuid)) {
                                sender.getTransceiver().setTransceiver(null);
                                sender.setTransceiver(null);
                                Util.say(player, "info.signal.disconnected");
                                updateTileEntity(senderRails.get(uuid));
                                updateTileEntity(sender);
                            } else {
                                sender.setTransceiver(senderRails.get(uuid));
                                sender.getTransceiver().setTransceiver(sender);
                                Util.say(player, "info.signal.connected");
                                updateTileEntity(senderRails.get(uuid));
                                updateTileEntity(sender);
                            }
                        }
                        senderRails.remove(uuid);
                    } else {
                        if (receiverRails.containsKey(uuid)) { //Here is actuator to transceiver, 1 to 1
                            if (receiverRails.get(uuid) instanceof TileEntityActuator actuator) {
                                if (actuator.getTarget() != sender) {
                                    actuator.setTarget(sender);
                                    Util.say(player, "info.signal.connected");
                                    updateTileEntity(actuator);
                                    updateTileEntity(sender);
                                } else {
                                    actuator.setTarget(null);
                                    Util.say(player, "info.signal.disconnected");
                                    updateTileEntity(actuator);
                                    updateTileEntity(sender);
                                }
                            } else {
                                Util.say(player, "info.signal.error");
                            }
                            receiverRails.remove(uuid);
                        } else {
                            senderRails.put(uuid, sender);
                            Util.say(player, "info.signal.begin");
                        }
                    }
                    return InteractionResult.SUCCESS;
                } else if (blockEntity instanceof TileEntityTransceiver transceiver) {
                    if (senderRails.containsKey(uuid)) {
                        if (senderRails.get(uuid) instanceof TileEntityMultiSender) {
                            if (transceiver.getTransceiver() == senderRails.get(uuid)) {
                                transceiver.getTransceiver().setTransceiver(null);
                                transceiver.setTransceiver(null);
                                Util.say(player, "info.signal.disconnected");
                                updateTileEntity(senderRails.get(uuid));
                                updateTileEntity(transceiver);
                            } else {
                                Util.say(player, "info.signal.error");
                            }
                        } else {
                            //Here is blocking rail, link with 1n4148 instead of this item, it may be dangerous
                            Util.say(player, "info.signal.abort");
                        }
                        senderRails.remove(uuid);
                    } else {
                        senderRails.put(uuid, transceiver);
                        Util.say(player, "info.signal.begin");
                    }
                    return InteractionResult.SUCCESS;
                } else if (blockEntity instanceof TileEntityReceiver receiver) {
                    if (senderRails.containsKey(uuid)) {
                        if (receiver.getSender() != senderRails.get(uuid)) {
                            receiver.setSender(senderRails.get(uuid));
                            if (senderRails.get(uuid) instanceof TileEntityMultiSender multiSender) {
                                multiSender.incTarget();
                            }
                            Util.say(player, "info.signal.connected");
                            updateTileEntity(senderRails.get(uuid));
                            updateTileEntity(receiver);
                        } else {
                            receiver.setSender(null);
                            if (senderRails.get(uuid) instanceof TileEntityMultiSender multiSender) {
                                multiSender.decTarget();
                            }
                            Util.say(player, "info.signal.disconnected");
                            updateTileEntity(senderRails.get(uuid));
                            updateTileEntity(receiver);
                        }
                        senderRails.remove(uuid);
                    } else {
                        if (receiverRails.containsKey(uuid)) {
                            if (receiverRails.get(uuid) instanceof TileEntityActuator actuator) {
                                if (actuator == receiver) {
                                    Util.say(player, "info.signal.abort");
                                } else if (actuator.getTarget() != receiver) {
                                    actuator.setTarget(receiver);
                                    Util.say(player, "info.signal.connected");
                                    updateTileEntity(actuator);
                                    updateTileEntity(receiver);
                                } else {
                                    actuator.setTarget(null);
                                    Util.say(player, "info.signal.disconnected");
                                    updateTileEntity(actuator);
                                    updateTileEntity(receiver);
                                }
                            } else if (receiver instanceof TileEntityActuator actuator) {
                                if (actuator.getTarget() != receiverRails.get(uuid)) {
                                    actuator.setTarget(receiverRails.get(uuid));
                                    Util.say(player, "info.signal.connected");
                                    updateTileEntity(receiverRails.get(uuid));
                                    updateTileEntity(receiver);
                                } else {
                                    actuator.setTarget(null);
                                    Util.say(player, "info.signal.disconnected");
                                    updateTileEntity(receiverRails.get(uuid));
                                    updateTileEntity(receiver);
                                }
                            }
                            receiverRails.remove(uuid);
                        } else {
                            if (receiver instanceof TileEntityPassiveReceiver) {
                                Util.say(player, "info.signal.error");
                            } else {
                                receiverRails.put(uuid, receiver);
                                Util.say(player, "info.signal.begin");
                            }
                        }
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    onConnectorUseLast(player, uuid, blockEntity);
                }
            }
        }

        return InteractionResult.FAIL;
    }
}
