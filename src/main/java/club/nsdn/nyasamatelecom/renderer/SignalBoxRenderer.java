package club.nsdn.nyasamatelecom.renderer;

import club.nsdn.nyasamatelecom.api.device.SignalBox;
import club.nsdn.nyasamatelecom.api.device.SignalBoxGetter;
import club.nsdn.nyasamatelecom.api.device.SignalBoxSender;
import club.nsdn.nyasamatelecom.api.render.AbsTileEntitySpecialRenderer;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityActuator;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityMultiSender;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * The signal box renderer, ported from the 1.12.2 {@code SignalBoxRenderer}. One instance serves eight
 * registered block entity types, exactly as in 1.12.2 (see {@code TelecomRenderers}).
 *
 * <p>Both 1.12.2 constructors survive, so the caller can still choose between the default shell texture and
 * a named one ({@code nsasm_box_base}, {@code signal_box_gt}, {@code signal_box_redstone},
 * {@code signal_box_wireless}, {@code delayer_base} are the ones the 1.12.2 model binder used). The
 * {@code hasButton} flag keeps deciding whether the button and its lamp are drawn at all -- which also
 * feeds the red indicator's 1.12.2 special case, {@code inverted && modelBtn == null ? SIGN_W : SIGN_R}.</p>
 *
 * <p>Port mechanics: the four {@code WavefrontObject} fields become model <b>ids</b> resolved through
 * {@link DeviceModels#resolve} at draw time (models do not exist when a renderer is constructed); the
 * texture {@code ResourceLocation}s keep their 1.12.2 shape via {@link DeviceModels#texture}; and the
 * outer {@code glTranslatef(x + 0.5F, y + 0.5F, z + 0.5F)} becomes a single
 * {@code translate(0.5, 0.5, 0.5)}, because the pose stack handed to a block entity renderer is already at
 * the block's origin. Every {@code GL11} push/pop pair is kept as a {@code PoseStack} push/pop pair, and the
 * {@code beginSpecialLighting()}/{@code endSpecialLighting()} calls are kept where 1.12.2 had them even
 * though they are no-ops now (render state is a property of the render type in 1.21.1).</p>
 */
public class SignalBoxRenderer extends AbsTileEntitySpecialRenderer {

    private static final int SIGN_R = 0;
    private static final int SIGN_Y = 1;
    private static final int SIGN_G = 2;
    private static final int SIGN_NONE = 3;
    private static final int SIGN_W = 4;

    private final ResourceLocation modelBase = DeviceModels.id("signal_box_base");
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

    public SignalBoxRenderer(boolean hasButton) {
        textureBase = DeviceModels.texture("signal_box_base.png");

        if (hasButton) {
            modelBtn = DeviceModels.id("signal_box_btn");
            modelBtnLight = DeviceModels.id("signal_box_btn_light");
        } else {
            modelBtn = null;
            modelBtnLight = null;
        }
    }

    public SignalBoxRenderer(boolean hasButton, String texture) {
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

        boolean txState;
        boolean rxState;
        boolean sgnState = (meta & 0x8) != 0;
        boolean isEnabled = false;

        boolean inverted = false;

        if (te instanceof TileEntityActuator) {
            txState = ((TileEntityActuator) te).getTarget() != null;
            rxState = ((TileEntityActuator) te).getSender() != null;
            if (te instanceof SignalBox.TileEntitySignalBox) {
                inverted = ((SignalBox.TileEntitySignalBox) te).inverterEnabled;
                sgnState = ((SignalBox.TileEntitySignalBox) te).isEnabled;
            }
        } else if (te instanceof TileEntityReceiver) {
            txState = false;
            rxState = ((TileEntityReceiver) te).getSender() != null;
            if (te instanceof SignalBoxGetter.TileEntitySignalBoxGetter) {
                sgnState = ((SignalBoxGetter.TileEntitySignalBoxGetter) te).isEnabled;
            }
        } else if (te instanceof TileEntityMultiSender && modelBtn == null) {
            txState = ((TileEntityMultiSender) te).targetCount > 0;
            rxState = false;
        } else if (te instanceof TileEntityMultiSender) {
            if (te instanceof SignalBoxSender.TileEntitySignalBoxSender) {
                isEnabled = ((SignalBoxSender.TileEntitySignalBoxSender) te).isEnabled;
            }
            txState = ((TileEntityMultiSender) te).targetCount > 0;
            rxState = ((TileEntityMultiSender) te).getTransceiver() != null;
        } else return;

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

        DeviceModels.renderPart(DeviceModels.resolve(models[SIGN_G]), textures[rxState ? SIGN_G : SIGN_NONE],
                poseStack, bufferSource, packedLight, packedOverlay);
        DeviceModels.renderPart(DeviceModels.resolve(models[SIGN_Y]), textures[txState ? SIGN_Y : SIGN_NONE],
                poseStack, bufferSource, packedLight, packedOverlay);
        DeviceModels.renderPart(DeviceModels.resolve(models[SIGN_R]),
                textures[
                        sgnState ? (
                                (inverted && modelBtn == null) ? SIGN_W : SIGN_R
                        ) : SIGN_NONE
                ], poseStack, bufferSource, packedLight, packedOverlay);

        poseStack.popPose();

        RendererHelper.endSpecialLighting();

        poseStack.popPose();
    }
}
