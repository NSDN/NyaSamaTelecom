package club.nsdn.nyasamatelecom.block;

import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The NyaSama Telecom sign panel, ported from the 1.12.2 {@code BlockSign}
 * ({@code BlockGlass(Material.GLASS, false)}, registry name {@code nyasamatelecom:nst_sign}).
 *
 * <p>See {@link BlockLogo} for the {@code BlockGlass} -&gt; {@link TransparentBlock} mapping and for why this
 * family has no {@code codec()} override.</p>
 */
public class BlockSign extends TransparentBlock {

    public BlockSign(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
