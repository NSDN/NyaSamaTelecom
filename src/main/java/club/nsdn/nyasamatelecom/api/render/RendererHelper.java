package club.nsdn.nyasamatelecom.api.render;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityActuator;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityReceiver;
import club.nsdn.nyasamatelecom.api.util.Util;
import cn.ac.nya.forgeobj.WavefrontObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created by drzzm32 on 2018.12.14.
 */
public class RendererHelper {

    public static void beginSpecialLightingNoDepth() {
        RenderHelper.disableStandardItemLighting();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        } else {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        GL11.glDepthMask(false);
    }

    public static void endSpecialLightingNoDepth() {
        GL11.glDepthMask(true);
        RenderHelper.enableStandardItemLighting();
    }

    public static void beginSpecialLighting() {
        RenderHelper.disableStandardItemLighting();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        } else {
            GL11.glShadeModel(GL11.GL_FLAT);
        }
    }

    public static void endSpecialLighting() {
        RenderHelper.enableStandardItemLighting();
    }

    public static void renderWithResourceAndRotation(WavefrontObject model, float angle, ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(0.0625F, 0.0625F, 0.0625F);
        GL11.glPushMatrix();
        GL11.glRotatef(angle, 0.0F, -1.0F, 0.0F);
        model.renderAll();
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    public static void renderWithResource(WavefrontObject model, ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(0.0625F, 0.0625F, 0.0625F);
        model.renderAll();
        GL11.glPopMatrix();
    }

    public static void renderPartWithResource(WavefrontObject model, String part, ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(0.0625F, 0.0625F, 0.0625F);
        model.renderPart(part);
        GL11.glPopMatrix();
    }

    public static void renderPartWithResourceAndRotation(WavefrontObject model, String part, float angle, ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(0.0625F, 0.0625F, 0.0625F);
        GL11.glPushMatrix();
        GL11.glRotatef(angle, 0.0F, -1.0F, 0.0F);
        model.renderPart(part);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    public static void renderOtherPartWithResourceAndRotation(WavefrontObject model, String part, float angle, ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(0.0625F, 0.0625F, 0.0625F);
        GL11.glPushMatrix();
        GL11.glRotatef(angle, 0.0F, -1.0F, 0.0F);
        model.renderAllExcept(part);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    public static void renderWithResource4(WavefrontObject model, ResourceLocation texture) {
        for (int i = 0; i < 4; i++)
            renderWithResourceAndRotation(model, 90.0F * i, texture);
    }

    public static void renderConnection(TileEntity src, TileEntity dst) {
        if (src == null || dst == null) return;

        Vec3d offset = new Vec3d(0.5, 0.5, 0.5);
        Vec3d srcPos = new Vec3d(src.getPos());
        Vec3d dstPos = new Vec3d(dst.getPos());
        srcPos = srcPos.add(offset); dstPos = dstPos.add(offset);

        Vec3d vec = dstPos.subtract(srcPos);
        LinkedList<Tuple<String, Double>> list = new LinkedList<>();
        list.add(new Tuple<>("x", vec.x));
        list.add(new Tuple<>("y", vec.y));
        list.add(new Tuple<>("z", vec.z));
        list.sort(Comparator.comparingDouble(value -> -Math.abs(value.getSecond())));

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        double h = vec.lengthVector() * Math.PI * 10.0; float a = 0.75F;
        Util.Color3 color = Util.hsv2RGB((float) h, 1.0F, 1.0F);

        Vec3d v = srcPos;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(v.x, v.y, v.z).color(color.r, color.g, color.b, a).endVertex();
        for (Tuple<String, Double> t : list) {
            String dir = t.getFirst();
            switch (dir) {
                default:
                case "y":
                    v = v.addVector(0, vec.y, 0);
                    break;
                case "x":
                    v = v.addVector(vec.x, 0, 0);
                    break;
                case "z":
                    v = v.addVector(0, 0, vec.z);
                    break;
            }
            buffer.pos(v.x, v.y, v.z).color(color.r, color.g, color.b, a).endVertex();
        }
        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopAttrib();
    }

    public static void renderConnection(TileEntityBase t) {
        if (t instanceof TileEntityReceiver) {
            TileEntityBase teb = ((TileEntityReceiver) t).getSender();
            if (teb != null) {
                renderConnection(teb, t);
            }
        }
        if (t instanceof TileEntityActuator) {
            TileEntity te = ((TileEntityActuator) t).getTarget();
            if (te != null) {
                renderConnection(t, te);
            }
        }
    }

}
