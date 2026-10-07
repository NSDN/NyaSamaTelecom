package club.nsdn.nyasamatelecom.api.render;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

/**
 * Ported from the 1.12.2 {@code AbsFastTESR}.
 *
 * <p>The 1.12.2 class extended Forge's {@code FastTESR<TileEntityBase>}, a performance-oriented
 * renderer variant with a reduced signature. <b>There is no fast path in 1.21.1</b> -- verified
 * against neoforge-21.1.256-merged.jar, {@code FastTESR} is gone and every block entity renderer
 * implements the single {@link BlockEntityRenderer} contract. The class is kept as an (empty) marker
 * so downstream renderers written against the old name keep compiling when they are ported, and so
 * the "was a fast TESR once" fact stays visible in the diff.</p>
 */
public abstract class AbsFastTESR implements BlockEntityRenderer<TileEntityBase> {
}
