package club.nsdn.nyasamatelecom.block;

import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The NSDN logo panel, ported from the 1.12.2 {@code BlockNSDNLogo}
 * ({@code BlockGlass(Material.GLASS, false)}, registry name {@code nyasamatelecom:nst_nsdn}).
 *
 * <p>See {@link BlockLogo} for the {@code BlockGlass} -&gt; {@link TransparentBlock} mapping and for why this
 * family has no {@code codec()} override.</p>
 */
public class BlockNSDNLogo extends TransparentBlock {

    public BlockNSDNLogo(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
