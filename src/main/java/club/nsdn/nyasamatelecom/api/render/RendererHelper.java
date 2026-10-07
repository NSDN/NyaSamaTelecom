package club.nsdn.nyasamatelecom.api.render;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityActuator;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Rendering helpers for NyaSama devices, ported from the 1.12.2 {@code RendererHelper}.
 *
 * <p><b>This class is where decision D4 lands.</b> In 1.12.2 every {@code renderWithResource*} method
 * took a {@code cn.ac.nya.forgeobj.WavefrontObject} and called {@code model.renderAll()},
 * {@code model.renderPart(part)} or {@code model.renderAllExcept(part)} after binding a texture and
 * pushing GL matrices. In 1.21.1 that whole custom loader is gone (D4: discard the 1.7.10-era
 * WavefrontObject pipeline and use the datapack model system instead), so the port maps onto baked
 * models:</p>
 * <ul>
 *   <li>a texture bind becomes a {@link RenderType} chosen by texture
 *       ({@code RenderType.entityCutout(texture)} etc.) -- the render type <i>is</i> the state;</li>
 *   <li>{@code GL11.glPushMatrix/glScalef/glRotatef} become {@link PoseStack} operations
 *       ({@code scale(0.0625)} and a Y rotation, matching the old rotate-about-{-Y} convention);</li>
 *   <li>drawing goes through {@link MultiBufferSource} and
 *       {@code ModelBlockRenderer#renderModel(PoseStack.Pose, VertexConsumer, BlockState, BakedModel,
 *       float, float, float, int, int)}.</li>
 * </ul>
 *
 * <p>The old {@code begin/endSpecialLighting[NoDepth]} pair has no equivalent and is intentionally a
 * no-op: blend, cull and depth state are properties of the render type in 1.21.1, not global GL state
 * that a caller switches on and off. The methods are kept so ported call sites keep compiling and so
 * the removal is visible in the diff.</p>
 *
 * <p><b>Deferred</b> (see {@code docs/08-OBJ-PIPELINE.md}): {@code renderPart*} and
 * {@code renderAllExcept*} relied on runtime per-group visibility inside the OBJ model. The modern
 * loader has no such feature -- named parts were therefore split into individual baked models
 * (Track B: 87 model groups expanded, 606 part models with textures and MTLs generated). Consuming
 * that part manifest is part of the optics round, where the renderers that actually need it live, so
 * these helpers are added then rather than guessed now.</p>
 */
public class RendererHelper {

    private RendererHelper() {
    }

    /** No-op in 1.21.1: lighting/blend/depth are render-type properties (kept for call-site parity). */
    public static void beginSpecialLighting() {
    }

    public static void endSpecialLighting() {
    }

    /** No-op in 1.21.1: the depth-mask trick is expressed by the chosen render type instead. */
    public static void beginSpecialLightingNoDepth() {
    }

    public static void endSpecialLightingNoDepth() {
    }

    /** 1.12.2 drew a whole OBJ scaled by 1/16 with the given texture bound. */
    public static void renderWithResource(BakedModel model, ResourceLocation texture, PoseStack poseStack,
                                          MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderModel(model, texture, poseStack, bufferSource, packedLight, packedOverlay);
    }

    /** Same, with the old rotate-about-{-Y} applied before drawing. */
    public static void renderWithResourceAndRotation(BakedModel model, float angle, ResourceLocation texture,
                                                     PoseStack poseStack, MultiBufferSource bufferSource,
                                                     int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(angle));
        renderModel(model, texture, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    /** 1.12.2: draw the model four times, 90 degrees apart. */
    public static void renderWithResource4(BakedModel model, ResourceLocation texture, PoseStack poseStack,
                                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        for (int i = 0; i < 4; i++) {
            renderWithResourceAndRotation(model, 90.0F * i, texture, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private static void renderModel(BakedModel model, ResourceLocation texture, PoseStack poseStack,
                                    MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        // The old code scaled by 1/16 because OBJ coordinates were in block units.
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                .renderModel(poseStack.last(), consumer, null, model, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
        poseStack.popPose();
    }

    /**
     * Debug visualization: a colour-coded orthogonal path from a device to its peer.
     *
     * <p>Ported from the 1.12.2 version, which walked the largest axis first (the sorted list below)
     * and coloured the line by {@code distance * PI * 10} through {@link Util#hsv2RGB}. The drawing
     * changes from immediate-mode {@code Tessellator}/{@code GL_LINE_STRIP} to a
     * {@link RenderType#lines()} buffer, but the geometry and colour rule are unchanged.</p>
     *
     * <p>Caller contract: the pose stack is expected to be positioned at the source block, exactly as
     * the 1.12.2 renderer translated before calling this.</p>
     */
    public static void renderConnection(BlockEntity src, BlockEntity dst, PoseStack poseStack, MultiBufferSource bufferSource) {
        if (src == null || dst == null) {
            return;
        }

        Vec3 offset = new Vec3(0.5, 0.5, 0.5);
        Vec3 srcPos = Vec3.atLowerCornerOf(src.getBlockPos()).add(offset);
        Vec3 dstPos = Vec3.atLowerCornerOf(dst.getBlockPos()).add(offset);
        Vec3 vec = dstPos.subtract(srcPos);

        // Largest absolute component first, so the path leaves the block along its dominant axis.
        List<double[]> order = new ArrayList<>();
        order.add(new double[] { 0, vec.x });
        order.add(new double[] { 1, vec.y });
        order.add(new double[] { 2, vec.z });
        order.sort(Comparator.comparingDouble(entry -> -Math.abs(entry[1])));

        double h = vec.length() * Math.PI * 10.0;
        float a = 0.75F;
        Util.Color3 color = Util.hsv2RGB((float) h, 1.0F, 1.0F);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        Vec3 v = srcPos;
        vertex(consumer, poseStack, v, color, a);
        for (double[] entry : order) {
            int axis = (int) entry[0];
            v = switch (axis) {
                case 1 -> v.add(0, vec.y, 0);
                case 2 -> v.add(0, 0, vec.z);
                default -> v.add(vec.x, 0, 0);
            };
            vertex(consumer, poseStack, v, color, a);
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack poseStack, Vec3 pos, Util.Color3 color, float alpha) {
        consumer.addVertex(poseStack.last(), (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(color.r, color.g, color.b, alpha)
                .setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
    }

    /** Picks the peer(s) of a device and draws the connection, as the 1.12.2 overload did. */
    public static void renderConnection(TileEntityBase blockEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        if (blockEntity instanceof TileEntityReceiver receiver) {
            TileEntityBase sender = receiver.getSender();
            if (sender != null) {
                renderConnection(sender, blockEntity, poseStack, bufferSource);
            }
        }
        if (blockEntity instanceof TileEntityActuator actuator) {
            BlockEntity target = actuator.getTarget();
            if (target != null) {
                renderConnection(blockEntity, target, poseStack, bufferSource);
            }
        }
    }
}
