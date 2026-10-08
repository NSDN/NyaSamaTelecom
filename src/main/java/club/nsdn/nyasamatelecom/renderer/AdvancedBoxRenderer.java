package club.nsdn.nyasamatelecom.renderer;

import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.tileblock.core.BlockRSLatch;
import club.nsdn.nyasamatelecom.tileblock.core.BlockTimer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * The RS latch / timing counter renderer, ported from the 1.12.2 {@code AdvancedBoxRenderer}: a
 * {@link SignalBoxRenderer} shell with a three-row, three-column character display and different lamp
 * semantics (the timer shows its set and elapsed counts, the latch shows its pending state).
 *
 * <h2>The character display</h2>
 * <p>1.12.2 drew each cell with
 * {@code RendererHelper.renderPartWithResource(modelLcd, "f" + (r + 1) + (c + 1), textureText[ch])} -- one
 * named object of {@code signal_box_lcd.obj} per cell, textured with one of the 128 glyph plates in
 * {@code textures/fonts/}. The ported helper has no part variant (the whole {@code renderPart*} family is
 * deferred, see {@code docs/08-OBJ-PIPELINE.md} §5), so each cell is resolved as its own model id
 * {@code signal_box_lcd__fXY} -- the pipeline's part naming -- through {@link DeviceModels#resolveOr}, which
 * falls back to the whole LCD model if the split has not been generated yet.</p>
 *
 * <p><b>NOT PORTED: the per-glyph colour tint.</b> 1.12.2 wrapped each cell in
 * {@code GL11.glColor3f(r, g, b)} and the three rows used red, grey and blue. The render type the frozen
 * helper builds ({@code RenderType.entityCutoutNoCull}) carries no free colour, and
 * {@code RendererHelper.renderWithResource*} exposes no colour parameter, so the tint has no place to go
 * without adding API to a frozen class. The glyphs are therefore drawn untinted (white plates tinted by
 * their own texture), and the colour-coded rows are lost. Fixing this needs either a colour-aware render
 * type in the helper or 128 pre-tinted glyph sets; both are follow-up work, not a translation.</p>
 *
 * <p>Two 1.12.2 quirks are transcribed rather than tidied: {@code doRenderText(int, int, int, char)}
 * contains {@code if (c < 0) r = 0;} where {@code c} was clearly meant (an unused-looking assignment to the
 * wrong variable that can turn a negative column into a clamped row), and the row/column clamping really is
 * {@code r, c ∈ [0, 2]}. {@code textureText[ch]} is indexed by the raw {@code char}, exactly as before --
 * the caller only ever formats digits and spaces, so nothing above index 127 reaches it.</p>
 */
public class AdvancedBoxRenderer extends AbsTileEntitySpecialRenderer {

    private static final int SIGN_R = 0;
    private static final int SIGN_Y = 1;
    private static final int SIGN_G = 2;
    private static final int SIGN_NONE = 3;
    private static final int SIGN_W = 4;

    private final ResourceLocation modelBase = DeviceModels.id("signal_box_base");
    private final ResourceLocation modelLcd = DeviceModels.id("signal_box_lcd");

    private final ResourceLocation modelBtn;
    private final ResourceLocation modelBtnLight;
    private final ResourceLocation textureBase;

    private final ResourceLocation[] models = {
            DeviceModels.id("signal_box_sign1"),
            DeviceModels.id("signal_box_sign2"),
            DeviceModels.id("signal_box_sign3"),
    };
    private final ResourceLocation[] textures = {
            DeviceModels.texture("signal_box_r.png"),
            DeviceModels.texture("signal_box_y.png"),
            DeviceModels.texture("signal_box_g.png"),
            DeviceModels.texture("signal_box_none.png"),
            DeviceModels.texture("signal_box_w.png"),
    };

    public AdvancedBoxRenderer(boolean hasButton, String texture) {
        textureBase = DeviceModels.texture(texture + ".png");

        if (hasButton) {
            modelBtn = DeviceModels.id("signal_box_btn");
            modelBtnLight = DeviceModels.id("signal_box_btn_light");
        } else {
            modelBtn = null;
            modelBtnLight = null;
        }
    }

    @Override
    public void renderDevice(TileEntityBase te, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int meta = te.META;

        boolean stateA = false;
        boolean stateB = false;
        boolean stateC = false;
        boolean isEnabled = false;
        boolean inverted = false;

        if (te instanceof BlockRSLatch.TileEntityRSLatch latch) {
            stateA = latch.isEnabled;
            stateB = latch.state == BlockRSLatch.TileEntityRSLatch.STATE_POS;
            stateC = latch.state == BlockRSLatch.TileEntityRSLatch.STATE_NEG;
            isEnabled = latch.isEnabled;
            inverted = latch.state == BlockRSLatch.TileEntityRSLatch.STATE_ZERO;
        } else if (te instanceof BlockTimer.TileEntityTimer timer) {
            stateA = timer.autoReload;
            stateB = timer.state == BlockTimer.TileEntityTimer.STATE_POS;
            stateC = timer.state == BlockTimer.TileEntityTimer.STATE_NEG;
            isEnabled = timer.isEnabled;
            inverted = timer.state == BlockTimer.TileEntityTimer.STATE_ZERO;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        poseStack.pushPose();
        DeviceModels.applyDeviceOrientation(poseStack, meta);

        DeviceModels.renderPart(DeviceModels.resolve(modelBase), textureBase, poseStack, bufferSource,
                packedLight, packedOverlay);
        if (modelBtn != null) {
            DeviceModels.renderPart(DeviceModels.resolve(modelBtn), textureBase, poseStack, bufferSource,
                    packedLight, packedOverlay);
        }

        RendererHelper.beginSpecialLighting();

        if (modelBtnLight != null) {
            DeviceModels.renderPart(DeviceModels.resolve(modelBtnLight),
                    textures[isEnabled ? SIGN_W : SIGN_NONE], poseStack, bufferSource,
                    packedLight, packedOverlay);
        }

        DeviceModels.renderPart(DeviceModels.resolve(models[SIGN_G]), textures[stateC ? SIGN_G : SIGN_NONE],
                poseStack, bufferSource, packedLight, packedOverlay);
        DeviceModels.renderPart(DeviceModels.resolve(models[SIGN_Y]), textures[stateB ? SIGN_Y : SIGN_NONE],
                poseStack, bufferSource, packedLight, packedOverlay);
        DeviceModels.renderPart(DeviceModels.resolve(models[SIGN_R]),
                textures[
                        stateA ? (
                                (inverted && modelBtn == null) ? SIGN_W : SIGN_R
                        ) : SIGN_NONE
                ], poseStack, bufferSource, packedLight, packedOverlay);

        if (te instanceof BlockTimer.TileEntityTimer timer) {
            doRenderText(0, 0, 0xFF1744, 'S', poseStack, bufferSource, packedLight, packedOverlay);
            doRenderText(0, 1, 0xCDCDCD, 'o', poseStack, bufferSource, packedLight, packedOverlay);
            doRenderText(0, 2, 0x2979FF, 'C', poseStack, bufferSource, packedLight, packedOverlay);

            doRenderText(1, 0xFF1744, String.format("%3d", timer.setTime),
                    poseStack, bufferSource, packedLight, packedOverlay);
            doRenderText(2, 0x2979FF, String.format("%3d", timer.tmpTime),
                    poseStack, bufferSource, packedLight, packedOverlay);
        }

        poseStack.popPose();

        RendererHelper.endSpecialLighting();

        poseStack.popPose();
    }

    /** 1.12.2 {@code doRenderText(int r, int color, String text)}: up to three characters, left to right. */
    private void doRenderText(int r, int color, String text, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (r < 0) r = 0;
        if (r > 2) r = 2;

        for (int c = 0; c < (text.length() > 3 ? 3 : text.length()); c++) {
            renderCell(r, c, text.charAt(c), poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    /** 1.12.2 {@code doRenderText(int r, int c, int color, char chr)}, clamping quirk included. */
    private void doRenderText(int r, int c, int color, char chr, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (r < 0) r = 0; if (r > 2) r = 2;
        // Transcribed 1.12.2 quirk: this assigns to r, not c, in both halves of the line.
        if (c < 0) r = 0; if (c > 2) r = 2;

        renderCell(r, c, chr, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderCell(int r, int c, char chr, PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay) {
        ResourceLocation cell = DeviceModels.id("signal_box_lcd__f" + (r + 1) + (c + 1));
        DeviceModels.renderPart(DeviceModels.resolveOr(cell, modelLcd), DeviceModels.font(chr),
                poseStack, bufferSource, packedLight, packedOverlay);
    }
}
