package club.nsdn.nyasamatelecom.renderer;

import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.tileblock.core.BlockNSPGA;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * The NSPGA renderer, ported from the 1.12.2 {@code NSPGARenderer}: the shared shell model plus a per-block
 * "print" plate that is textured with the block's own {@code <name>_print.png}.
 *
 * <p>The lazy texture allocation is preserved exactly, including its mutability: 1.12.2 wrote
 * {@code block.texturePrint = new ResourceLocation(...)} on first render and read it on every render
 * afterwards. The field used to be {@code @SideOnly(CLIENT)}; the annotation is forbidden in this port and
 * unnecessary (it only controlled the field's presence in the dedicated-server jar), so it is a plain field
 * on the block, documented at its declaration. The guard
 * {@code if (!(te.getBlockType() instanceof BlockNSPGA)) return;} is the same test written against
 * {@code te.getBlockState().getBlock()}.</p>
 */
public class NSPGARenderer extends AbsTileEntitySpecialRenderer {

    private final ResourceLocation modelBase = DeviceModels.id("nspga_base");
    private final ResourceLocation textureBase = DeviceModels.texture("nspga_base.png");

    private final ResourceLocation modelPrint = DeviceModels.id("nspga_print");

    @Override
    public void renderDevice(TileEntityBase te, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int meta = te.META;
        if (!(te.getBlockState().getBlock() instanceof BlockNSPGA block)) return;
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
