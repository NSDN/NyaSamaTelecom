package club.nsdn.nyasamatelecom.api.render;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

/**
 * Base renderer for NyaSama devices, ported from the 1.12.2
 * {@code AbsTileEntitySpecialRenderer}.
 *
 * <p>This is where the render contract changes most between versions, so the mapping is explicit:</p>
 * <ul>
 *   <li>{@code TileEntitySpecialRenderer<TileEntity>} becomes
 *       {@link BlockEntityRenderer}{@code <TileEntityBase>}. The generic collapses because 1.21.1 no
 *       longer needs the "render any TileEntity, then narrow" trick: the renderer is registered for a
 *       specific block entity type.</li>
 *   <li>The old {@code render(te, x, y, z, partialTicks, destroyStage, partial)} split into two
 *       same-named methods (one for {@code TileEntity}, one abstract for {@code TileEntityBase}).
 *       Java generics do not allow that overload any more, so the abstract half is renamed
 *       {@link #renderDevice}: subclasses implement that, and {@link #render} calls it.</li>
 *   <li>Coordinates arrive as a world-space {@link PoseStack} instead of raw {@code x}/{@code y}/{@code z},
 *       and drawing goes through {@link MultiBufferSource} rather than {@code GlStateManager} -- the
 *       immediate-mode GL calls of 1.12.2 no longer exist.</li>
 * </ul>
 *
 * <p><b>Deferred</b> (marked rather than dropped): the 1.12.2 renderer also drew "connection" lines
 * whenever the player wore a {@code NyaGameMR} helmet item, via
 * {@code RendererHelper.renderConnection(...)}. Both the item ({@code item/NyaGameMR}) and
 * {@code RendererHelper} belong to parts of this API package that are not ported yet, so the hook is
 * left out here and will be restored together with them.</p>
 */
public abstract class AbsTileEntitySpecialRenderer implements BlockEntityRenderer<TileEntityBase> {

    @Override
    public void render(TileEntityBase blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        this.renderDevice(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    /** The 1.12.2 abstract {@code render(TileEntityBase, ...)} slot. */
    public abstract void renderDevice(TileEntityBase blockEntity, float partialTick, PoseStack poseStack,
                                      MultiBufferSource bufferSource, int packedLight, int packedOverlay);
}
