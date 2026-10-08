package club.nsdn.nyasamatelecom.block;

import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The NyaSama Telecom logo panel, ported from the 1.12.2 {@code BlockLogo}.
 *
 * <p>1.12.2 extended {@code BlockGlass(Material.GLASS, false)} and set the registry name
 * {@code nyasamatelecom:nst_logo}; 1.21.1's equivalent base class is
 * {@link TransparentBlock} (vanilla merged {@code BlockGlass} into it) and the registry name now comes
 * from the {@code DeferredBlock} that constructs this class, so it disappears from here. All the 1.12.2
 * block properties (hardness 2.0, resistance 10.0, light level 1 -- i.e. 15, glass sound, glass material)
 * are supplied by {@code TelecomBlocks#glassProps()}, which is where they belong now that
 * {@code BlockBehaviour.Properties} is passed in rather than mutated in the constructor.</p>
 *
 * <p>No {@code codec()} override: the inherited {@code TransparentBlock.CODEC} is the correct codec for a
 * class with no extra properties, so this class is deliberately absent from any {@code BLOCK_TYPES}
 * registration. See {@code TelecomBlocks}' javadoc for the rule.</p>
 */
public class BlockLogo extends TransparentBlock {

    public BlockLogo(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
