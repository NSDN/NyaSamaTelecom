package club.nsdn.nyasamatelecom.renderer;

import club.nsdn.nyasamatelecom.api.device.TriStateSignalBox;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * The three-state signal box renderer, ported from the 1.12.2 {@code TriStateSignalBoxRenderer}.
 *
 * <p>Same shape as {@link SignalBoxRenderer} but with a four-plate indicator set and its own shell texture:
 * the receiver lamp is green, the transmitter lamp yellow, the signal lamp red (white when inverted) and
 * the tri-state lamp is white for negative and blue for positive -- and, unlike the other renderers, the
 * tri-state plate is drawn <b>unconditionally</b>, which is the 1.12.2 behaviour and stays that way.</p>
 *
 * <p>Port mechanics are identical to {@link SignalBoxRenderer}: model ids instead of {@code WavefrontObject}
 * fields, a single {@code translate(0.5, 0.5, 0.5)} in place of the {@code x}/{@code y}/{@code z} offset,
 * {@code PoseStack} push/pop pairs for the GL matrix stack, and the (now no-op) special-lighting pair kept
 * where the original had it.</p>
 */
public class TriStateSignalBoxRenderer extends AbsTileEntitySpecialRenderer {

    private static final int SIGN_R = 0;
    private static final int SIGN_Y = 1;
    private static final int SIGN_G = 2;
    private static final int SIGN_NONE = 3;
    private static final int SIGN_W = 4;
    private static final int SIGN_B = 5;

    private static final int LIGHT_SGN = 0;
    private static final int LIGHT_TXD = 1;
    private static final int LIGHT_RXD = 2;
    private static final int LIGHT_TRI = 3;

    private final ResourceLocation modelBase = DeviceModels.id("signal_box_base");
    private final ResourceLocation textureBase = DeviceModels.texture("tri_state_signal_box_base.png");

    private final ResourceLocation[] models = {
            DeviceModels.id("signal_box_sign1"),
            DeviceModels.id("signal_box_sign2"),
            DeviceModels.id("signal_box_sign3"),
            DeviceModels.id("signal_box_sign4"),
    };
    private final ResourceLocation[] textures = {
            DeviceModels.texture("signal_box_r.png"),
            DeviceModels.texture("signal_box_y.png"),
            DeviceModels.texture("signal_box_g.png"),
            DeviceModels.texture("signal_box_none.png"),
            DeviceModels.texture("signal_box_w.png"),
            DeviceModels.texture("signal_box_b.png"),
    };

    @Override
    public void renderDevice(TileEntityBase te, float partialTick, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int meta = te.META;

        boolean txState = false;
        boolean rxState = false;
        boolean sgnState = (meta & 0x8) != 0;

        boolean inverted = false;

        boolean triStateIsNeg = false;

        if (te instanceof TriStateSignalBox.TileEntityTriStateSignalBox triState) {
            txState = triState.getTarget() != null;
            rxState = triState.getSender() != null;
            inverted = triState.inverterEnabled;
            triStateIsNeg = triState.triStateIsNeg;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        poseStack.pushPose();
        DeviceModels.applyDeviceOrientation(poseStack, meta);

        DeviceModels.renderPart(DeviceModels.resolve(modelBase), textureBase, poseStack, bufferSource,
                packedLight, packedOverlay);

        RendererHelper.beginSpecialLighting();

        DeviceModels.renderPart(DeviceModels.resolve(models[LIGHT_RXD]),
                textures[rxState ? SIGN_G : SIGN_NONE], poseStack, bufferSource, packedLight, packedOverlay);
        DeviceModels.renderPart(DeviceModels.resolve(models[LIGHT_TXD]),
                textures[txState ? SIGN_Y : SIGN_NONE], poseStack, bufferSource, packedLight, packedOverlay);
        DeviceModels.renderPart(DeviceModels.resolve(models[LIGHT_SGN]),
                textures[
                        sgnState ? (
                                (inverted) ? SIGN_W : SIGN_R
                        ) : SIGN_NONE
                ], poseStack, bufferSource, packedLight, packedOverlay);
        DeviceModels.renderPart(DeviceModels.resolve(models[LIGHT_TRI]),
                textures[triStateIsNeg ? SIGN_W : SIGN_B], poseStack, bufferSource, packedLight, packedOverlay);

        poseStack.popPose();

        RendererHelper.endSpecialLighting();

        poseStack.popPose();
    }
}
