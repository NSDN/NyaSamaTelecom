package club.nsdn.nyasamatelecom.renderer;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.api.render.RendererHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

/**
 * Model and texture identifiers for the telecom block entity renderers, plus the two pieces of shared
 * geometry logic they all need. This class replaces the 1.12.2 {@code new WavefrontObject(new
 * ResourceLocation("nyasamatelecom", "models/blocks/<stem>.obj"))} call that each renderer made in its
 * field initialisers (decision D4: the custom OBJ loader is gone, and the NeoForge OBJ loader is reached
 * through the datapack model system instead).
 *
 * <h2>How a model id replaces a {@code WavefrontObject}</h2>
 * <p>{@code models/blocks/<stem>.obj} in 1.12.2 becomes the model id
 * {@code nyasamatelecom:block/<stem>}, i.e. the {@code models/} prefix and the {@code .obj} suffix are
 * dropped and the folder is the singular {@code block} (which is where the assets ship:
 * {@code assets/nyasamatelecom/models/block/<stem>.obj} plus the generated {@code neoforge:obj} wrapper
 * {@code <stem>.json}). This is the same convention {@code nyasamaoptics}' ported
 * {@code DeviceModels} uses.</p>
 *
 * <p>Because such a model is not reached through a blockstate, it must be handed to the loader explicitly:
 * {@code TelecomRenderers} fires {@code ModelEvent.RegisterAdditional} once per id returned by
 * {@link #catalogue()}, and {@link #resolve(ResourceLocation)} looks the same id up with
 * {@link ModelResourceLocation#standalone}. Missing ids resolve to the missing model and simply draw
 * nothing, which is the behaviour a failed {@code WavefrontObject} load had in 1.12.2.</p>
 *
 * <h2>Textures are addressed verbatim</h2>
 * <p>In 1.21.1 a render type's texture {@code ResourceLocation} is resolved <b>as written</b> against
 * {@code assets/<namespace>/<path>} (verified with {@code javap}: {@code SimpleTexture} calls
 * {@code ResourceManager.getResourceOrThrow(location)} and nothing adds a {@code textures/} prefix or a
 * {@code .png} suffix). The 1.12.2 renderers wrote full paths with extension, so the port keeps that shape
 * and only changes the folder to the singular {@code block} the assets actually use --
 * {@link #texture(String)} therefore builds {@code nyasamatelecom:textures/block/<file>}.</p>
 */
public final class DeviceModels {

    private static final String NAMESPACE = NyaSamaTelecom.MODID;
    private static final String MODEL_FOLDER = "block";

    private DeviceModels() {
    }

    /** The id of the model whose 1.12.2 file was {@code models/blocks/<name>.obj}. */
    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, MODEL_FOLDER + "/" + name);
    }

    /** A BER texture: 1.12.2 {@code nyasamatelecom:textures/blocks/<file>}, now singular. */
    public static ResourceLocation texture(String file) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/block/" + file);
    }

    /** One of the 128 glyph plates {@code AdvancedBoxRenderer} and the helmet HUD print with. */
    public static ResourceLocation font(int code) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/fonts/font_" + code + ".png");
    }

    /**
     * Resolves a model id to its baked model, or to the missing model when it is not loaded.
     *
     * <p>Deliberately <b>not</b> cached: {@code ModelManager#getModel} is a map lookup, and a cache here
     * would go stale on every resource reload unless something invalidated it. {@code ModelManager#getModel}
     * throws for an unknown id rather than returning null, so a failure is caught and turned into the
     * missing model -- drawing nothing, as 1.12.2 did when an OBJ failed to load.</p>
     */
    public static BakedModel resolve(ResourceLocation id) {
        try {
            return Minecraft.getInstance().getModelManager()
                    .getModel(ModelResourceLocation.standalone(id));
        } catch (RuntimeException e) {
            return Minecraft.getInstance().getModelManager().getMissingModel();
        }
    }

    /**
     * Resolves {@code id}, or {@code fallback} when {@code id} did not load.
     *
     * <p>Used for the one place where a part model is optional: {@code signal_box_lcd}'s nine cells are
     * produced by the OBJ pipeline's part-split, and if that step has not run yet the whole LCD model is a
     * better answer than a missing texture. This is a fallback, not a runtime part-visibility feature --
     * 1.21.1's OBJ loader has none (see {@code docs/08-OBJ-PIPELINE.md} §5).</p>
     */
    public static BakedModel resolveOr(ResourceLocation id, ResourceLocation fallback) {
        BakedModel model = resolve(id);
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            return resolve(fallback);
        }
        return model;
    }

    /**
     * Draws one model with the given texture.
     *
     * <p>1.12.2 called {@code RendererHelper.renderWithResourceAndRotation(model, 0, texture)} for every
     * one of these, and the ported helper needs the pose stack and the buffer source, which the 1.12.2
     * helper got from global GL state. A rotation of 0 is the identity, so
     * {@link RendererHelper#renderWithResource} is called directly. The per-part variant that
     * {@code AdvancedBoxRenderer} needs lives here because the ported helper has no
     * {@code renderPartWithResource} (see {@code docs/08-OBJ-PIPELINE.md} §5: the 1.21.1 OBJ loader cannot
     * switch parts on and off, so each part is its own baked model instead).</p>
     */
    public static void renderPart(BakedModel model, ResourceLocation texture, PoseStack poseStack,
                                  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        RendererHelper.renderWithResource(model, texture, poseStack, bufferSource, packedLight, packedOverlay);
    }

    /**
     * The 1.12.2 {@code switch (meta & 0x7)} orientation block, shared by every device renderer that had it.
     *
     * <p>Transcribed call for call and in the same order: the pose stack composes the same way GL's matrix
     * stack did, so a 1:1 sequence of {@code glTranslatef}/{@code glRotatef} becomes a 1:1 sequence of
     * {@code translate}/{@code mulPose}. Note that the rotations really are about the negative axes for
     * cases 0-3 ({@code GL11.glRotatef(a, 0, -1, 0)}), which is why the axis vectors below are passed
     * through unchanged rather than being "normalised" to the positive axis.</p>
     */
    public static void applyDeviceOrientation(PoseStack poseStack, int meta) {
        switch (meta & 0x7) {
            case 0:
                poseStack.mulPose(rotation(0.0F, 0.0F, -1.0F, 0.0F));
                break;
            case 1:
                poseStack.mulPose(rotation(90.0F, 0.0F, -1.0F, 0.0F));
                break;
            case 2:
                poseStack.mulPose(rotation(180.0F, 0.0F, -1.0F, 0.0F));
                break;
            case 3:
                poseStack.mulPose(rotation(270.0F, 0.0F, -1.0F, 0.0F));
                break;
            case 4:
                poseStack.translate(0.0F, 1.0F, 0.0F);
                poseStack.mulPose(rotation(90.0F, -1.0F, 0.0F, 0.0F));
                poseStack.translate(0.0F, -1.0F, 0.0F);
                poseStack.mulPose(rotation(0.0F, 0.0F, -1.0F, 0.0F));
                poseStack.translate(0.0F, 1.0F, -1.0F);
                break;
            case 5:
                poseStack.translate(0.0F, 1.0F, 0.0F);
                poseStack.mulPose(rotation(90.0F, 0.0F, 0.0F, -1.0F));
                poseStack.translate(0.0F, -1.0F, 0.0F);
                poseStack.mulPose(rotation(90.0F, 0.0F, -1.0F, 0.0F));
                poseStack.translate(0.0F, 1.0F, -1.0F);
                break;
            case 6:
                poseStack.translate(0.0F, 1.0F, 0.0F);
                poseStack.mulPose(rotation(90.0F, 1.0F, 0.0F, 0.0F));
                poseStack.translate(0.0F, -1.0F, 0.0F);
                poseStack.mulPose(rotation(180.0F, 0.0F, -1.0F, 0.0F));
                poseStack.translate(0.0F, 1.0F, -1.0F);
                break;
            case 7:
                poseStack.translate(0.0F, 1.0F, 0.0F);
                poseStack.mulPose(rotation(90.0F, 0.0F, 0.0F, 1.0F));
                poseStack.translate(0.0F, -1.0F, 0.0F);
                poseStack.mulPose(rotation(270.0F, 0.0F, -1.0F, 0.0F));
                poseStack.translate(0.0F, 1.0F, -1.0F);
                break;
            default:
                break;
        }
    }

    /** {@code GL11.glRotatef(degrees, ax, ay, az)} as a quaternion; the axis is used as given. */
    private static Quaternionf rotation(float degrees, float ax, float ay, float az) {
        return new Quaternionf().rotationAxis((float) Math.toRadians(degrees), ax, ay, az);
    }

    /**
     * Every model id the renderers in this package can ask for, so the registration step can drive
     * {@code ModelEvent.RegisterAdditional} from one place instead of grepping the renderers.
     *
     * <p>The first ten are whole OBJ models (Track A of {@code docs/08-OBJ-PIPELINE.md}). The nine
     * {@code signal_box_lcd__fXY} entries are Track B: 1.12.2's {@code AdvancedBoxRenderer} drew single
     * named objects out of {@code signal_box_lcd.obj} ({@code renderPartWithResource(modelLcd, "f11"...
     * "f33")}), and since 1.21.1 cannot hide objects inside a loaded model, that file is split into one
     * model per object with the pipeline's {@code <stem>__<part>} naming rule.</p>
     */
    public static ResourceLocation[] catalogue() {
        String[] names = {
                // SignalBoxRenderer / AdvancedBoxRenderer shell, button and indicator plates
                "signal_box_base", "signal_box_btn", "signal_box_btn_light",
                "signal_box_sign1", "signal_box_sign2", "signal_box_sign3", "signal_box_sign4",
                // AdvancedBoxRenderer LCD (whole model, kept for the fallback path)
                "signal_box_lcd",
                // NSPGARenderer / NSPGAFlexRenderer
                "nspga_base", "nspga_print",
                // AdvancedBoxRenderer LCD glyph cells (Track B parts of signal_box_lcd.obj)
                "signal_box_lcd__f11", "signal_box_lcd__f12", "signal_box_lcd__f13",
                "signal_box_lcd__f21", "signal_box_lcd__f22", "signal_box_lcd__f23",
                "signal_box_lcd__f31", "signal_box_lcd__f32", "signal_box_lcd__f33",
        };
        ResourceLocation[] ids = new ResourceLocation[names.length];
        for (int i = 0; i < names.length; i++) {
            ids[i] = id(names[i]);
        }
        return ids;
    }
}
