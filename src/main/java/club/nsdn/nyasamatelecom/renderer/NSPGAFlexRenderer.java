package club.nsdn.nyasamatelecom.renderer;

import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.tileblock.core.BlockNSPGAFlex;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * The flex NSPGA renderer, ported from the 1.12.2 {@code NSPGAFlexRenderer}.
 *
 * <p>Identical to {@link NSPGARenderer} except for which block it guards on; the two were separate classes
 * with separate code in 1.12.2 and are kept that way, because each is registered against a different block
 * entity type.</p>
 */
public class NSPGAFlexRenderer extends AbsTileEntitySpecialRenderer {

    private final ResourceLocation modelBase = DeviceModels.id("nspga_base");
    private final ResourceLocation textureBase = DeviceModels.texture("nspga_base.png");

    private final ResourceLocation modelPrint = DeviceModels.id("nspga_print");

    @Override
    public void renderDevice(TileEntityBase te, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int meta = te.META;
        if (!(te.getBlockState().getBlock() instanceof BlockNSPGAFlex block)) return;
        if (block.texturePrint == null)
            block.texturePrint = DeviceModels.texture(block.name + "_print" + ".png");

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        poseStack.pushPose();
        DeviceModels.applyDeviceOrientation(poseStack, meta);

        DeviceModels.renderPart(DeviceModels.resolve(modelBase), textureBase, poseStack, bufferSource,
                packedLight, packedOverlay);
        DeviceModels.renderPart(DeviceModels.resolve(modelPrint), block.texturePrint, poseStack, bufferSource,
                packedLight, packedOverlay);

        poseStack.popPose();

        RendererHelper.endSpecialLighting();

        poseStack.popPose();
    }
}
