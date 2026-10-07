package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.thewdj.telecom.IPassive;

/**
 * Ported from the 1.12.2 {@code TileEntityPassiveReceiver}.
 *
 * <p>The original comments are worth keeping, because they explain why this nearly empty class
 * exists:</p>
 * <pre>
 * // signal box should control this receiver
 * // receiver should use META &amp; 0x8 as enabled/powered flag
 * // but this device is different from actuator
 * </pre>
 * <p>So: same "controlled by a signal box" role as {@link TileEntityActuator}, but the device reacts
 * to {@code META &amp; 0x8} itself instead of having an actuator write to a target.</p>
 */
public class TileEntityPassiveReceiver extends TileEntityReceiver implements IPassive {

    public TileEntityPassiveReceiver(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
