package club.nsdn.nyasamatelecom.api.tool;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityActuator;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityTransceiver;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Device editor tool, ported from the 1.12.2 {@code DevEditor}.
 *
 * <p>This is the "copy one device's link, paste it onto another" tool. Its behaviour is carried over
 * exactly -- including the interaction of the two static maps:</p>
 * <ul>
 *   <li>sneaking: first click copies an actuator's <i>target</i>, second click pastes it onto another
 *       actuator;</li>
 *   <li>not sneaking: first click copies a receiver's <i>sender</i>, second click pastes it;</li>
 *   <li>clicking empty space clears both entries for that player.</li>
 * </ul>
 *
 * <p>Mechanical changes: {@code onItemUseFirst(player, world, pos, facing, ...)} becomes
 * {@code onItemUseFirst(ItemStack, UseOnContext)} (level/pos/player all come from the context),
 * {@code player.getUniqueID()} becomes {@code player.getUUID()}, {@code world.getTileEntity(pos)}
 * becomes {@code level.getBlockEntity(pos)}, and the stored values are {@link BlockEntity} rather
 * than {@code TileEntity}.</p>
 *
 * <p>Note the maps are {@code static}: the 1.12.2 code made them per-instance in the constructor but
 * declared them static, so the last constructed tool re-initialised the shared maps. That is preserved
 * (the subclasses in each mod construct their own editor), and it is why the note is here rather than
 * a silent change.</p>
 */
public class DevEditor extends ToolBase {

    public static LinkedHashMap<UUID, TileEntityTransceiver> sender;
    public static LinkedHashMap<UUID, BlockEntity> target;

    public DevEditor(Item.Properties properties, Tier tier) {
        super(properties, tier);
        sender = new LinkedHashMap<>();
        target = new LinkedHashMap<>();
    }

    @Override
    public InteractionResult onItemUseFirst(net.minecraft.world.item.ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        UUID uuid = player.getUUID();

        if (!level.isClientSide) {
            if (blockEntity == null) {
                sender.remove(uuid);
                target.remove(uuid);
                Util.say(player, "info.editor.clear");
                return InteractionResult.SUCCESS;
            }

            if (player.isShiftKeyDown()) {
                if (!target.containsKey(uuid)) {
                    if (blockEntity instanceof TileEntityActuator actuator) {
                        target.put(uuid, actuator.getTarget());
                        Util.say(player, "info.editor.copy.target");
                    } else {
                        Util.say(player, "info.editor.error");
                    }
                } else {
                    if (blockEntity instanceof TileEntityActuator actuator) {
                        actuator.setTarget(target.get(uuid));
                        updateTileEntity(blockEntity);
                        Util.say(player, "info.editor.paste.target");
                    } else {
                        Util.say(player, "info.editor.error");
                    }
                }
            } else {
                if (!sender.containsKey(uuid)) {
                    if (blockEntity instanceof TileEntityReceiver receiver) {
                        sender.put(uuid, receiver.getSender());
                        Util.say(player, "info.editor.copy.sender");
                    } else {
                        Util.say(player, "info.editor.error");
                    }
                } else {
                    if (blockEntity instanceof TileEntityReceiver receiver) {
                        receiver.setSender(sender.get(uuid));
                        updateTileEntity(blockEntity);
                        Util.say(player, "info.editor.paste.sender");
                    } else {
                        Util.say(player, "info.editor.error");
                    }
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }
}
