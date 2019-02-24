package club.nsdn.nyasamatelecom.renderer;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by drzzm32 on 2019.2.23.
 */
public class RenderNyaGameMR extends ModelBiped {

    public static final RenderNyaGameMR MODEL = new RenderNyaGameMR();
    public static final RenderNyaGameMR INSTANCE = MODEL;

    public static void drawTexturedModalRect(int x, int y, int z, int w, int h) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos((double)(x + 0), (double)(y + h), (double)z).tex(0, 1).endVertex();
        buffer.pos((double)(x + w), (double)(y + h), (double)z).tex(1, 1).endVertex();
        buffer.pos((double)(x + w), (double)(y + 0), (double)z).tex(1, 0).endVertex();
        buffer.pos((double)(x + 0), (double)(y + 0), (double)z).tex(0, 0).endVertex();
        tessellator.draw();
    }

    public static void drawTexturedColoredModalRect(int x, int y, int z, int w, int h, int color) {
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF, a = 192;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos((double)(x + 0), (double)(y + h), (double)z).tex(0, 1).color(r, g, b, a).endVertex();
        buffer.pos((double)(x + w), (double)(y + h), (double)z).tex(1, 1).color(r, g, b, a).endVertex();
        buffer.pos((double)(x + w), (double)(y + 0), (double)z).tex(1, 0).color(r, g, b, a).endVertex();
        buffer.pos((double)(x + 0), (double)(y + 0), (double)z).tex(0, 0).color(r, g, b, a).endVertex();
        tessellator.draw();
    }

    ResourceLocation hudBack = new ResourceLocation(NyaSamaTelecom.MODID, "textures/item/nyagame_mr_hud.png");

    public void drawHUDBack() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(hudBack);
        drawTexturedModalRect(0, 0, 0, 128, 64);
    }

    public int WIDTH = 29, HEIGH = 13, OFFSET = 6, SIZE = 4;

    static ResourceLocation textureText[];

    static {
        textureText = new ResourceLocation[128];
        for (int i = 0; i < 128; i++)
            textureText[i] =  new ResourceLocation(
                    NyaSamaTelecom.MODID, "textures/fonts/" + "font_" + i + ".png"
            );
    }

    void renderChar(int x, int y, char c, int color) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureText[c]);
        drawTexturedColoredModalRect(x, y, 0, SIZE, SIZE, color);
    }

    void renderString(String str, int x, int y, int color) {
        for (int i = 0; i < str.length(); i++)
            renderChar(x + i * SIZE, y, str.charAt(i), color);
    }

    String[] buffer = new String[HEIGH];

    public void drawHUDString() {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == null) buffer[i] = "";
            renderString(
                    buffer[i].substring(0, buffer[i].length() > WIDTH ? WIDTH : buffer[i].length()),
                    OFFSET, OFFSET + i * SIZE, 0x84FFFF
            );
        }
    }

    int ptr = 0;

    public void clear() {
        buffer = new String[HEIGH];
        ptr = 0;
    }

    public void print(String format, Object... args) {
        if (ptr >= HEIGH) {
            ptr = HEIGH - 1;
            buffer = Arrays.copyOfRange(buffer, 1, HEIGH + 1);
        }
        buffer[ptr] = String.format(format, args);
        ptr += 1;
    }

    LinkedList<ModelRenderer> models = new LinkedList<>();

    ModelRenderer GlassL;
    ModelRenderer GlassR;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Back;
    ModelRenderer PrShape1;
    ModelRenderer PrShape2;
    ModelRenderer PrShape3;
    ModelRenderer PrShape4;
    ModelRenderer PlShape1;
    ModelRenderer PlShape2;
    ModelRenderer PlShape3;
    ModelRenderer PlShape4;

    public RenderNyaGameMR() {
        super();

        textureWidth = 64;
        textureHeight = 32;

        GlassL = new ModelRenderer(this, 32, 16);
        GlassL.addBox(0F, 0F, 0F, 2, 1, 1);
        GlassL.setRotationPoint(1F, -5F, -6F);
        GlassL.setTextureSize(64, 32);
        GlassL.mirror = true;
        setRotation(GlassL, 0F, 0F, 0F);
        GlassR = new ModelRenderer(this, 32, 18);
        GlassR.addBox(0F, 0F, 0F, 2, 1, 1);
        GlassR.setRotationPoint(-3F, -5F, -6F);
        GlassR.setTextureSize(64, 32);
        GlassR.mirror = true;
        setRotation(GlassR, 0F, 0F, 0F);
        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 10, 1, 10);
        Shape1.setRotationPoint(-5F, -10F, -5F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 0);
        Shape2.addBox(0F, 0F, 0F, 2, 6, 1);
        Shape2.setRotationPoint(-5F, -9F, -6F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 0);
        Shape3.addBox(0F, 0F, 0F, 2, 6, 1);
        Shape3.setRotationPoint(3F, -9F, -6F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 0);
        Shape4.addBox(0F, 0F, 0F, 2, 1, 1);
        Shape4.setRotationPoint(-1F, -5F, -6F);
        Shape4.setTextureSize(64, 32);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 0);
        Shape5.addBox(0F, 0F, 0F, 6, 1, 1);
        Shape5.setRotationPoint(-3F, -4F, -6F);
        Shape5.setTextureSize(64, 32);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 0, 0);
        Shape6.addBox(0F, 0F, 0F, 6, 4, 1);
        Shape6.setRotationPoint(-3F, -9F, -6F);
        Shape6.setTextureSize(64, 32);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 0, 0);
        Shape7.addBox(0F, 0F, 0F, 10, 9, 1);
        Shape7.setRotationPoint(-5F, -9F, 5F);
        Shape7.setTextureSize(64, 32);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Back = new ModelRenderer(this, 0, 16);
        Back.addBox(0F, 0F, 0F, 9, 9, 0);
        Back.setRotationPoint(-4.5F, -9F, 6.1F);
        Back.setTextureSize(64, 32);
        Back.mirror = true;
        setRotation(Back, 0F, 0F, 0F);
        PrShape1 = new ModelRenderer(this, 0, 0);
        PrShape1.addBox(0F, 0F, 0F, 1, 6, 10);
        PrShape1.setRotationPoint(-6F, -9F, -5F);
        PrShape1.setTextureSize(64, 32);
        PrShape1.mirror = true;
        setRotation(PrShape1, 0F, 0F, 0F);
        PrShape2 = new ModelRenderer(this, 0, 0);
        PrShape2.addBox(0F, 0F, 0F, 1, 1, 8);
        PrShape2.setRotationPoint(-6F, -3F, -3F);
        PrShape2.setTextureSize(64, 32);
        PrShape2.mirror = true;
        setRotation(PrShape2, 0F, 0F, 0F);
        PrShape3 = new ModelRenderer(this, 0, 0);
        PrShape3.addBox(0F, 0F, 0F, 1, 1, 5);
        PrShape3.setRotationPoint(-6F, -2F, 0F);
        PrShape3.setTextureSize(64, 32);
        PrShape3.mirror = true;
        setRotation(PrShape3, 0F, 0F, 0F);
        PrShape4 = new ModelRenderer(this, 0, 0);
        PrShape4.addBox(0F, 0F, 0F, 1, 1, 2);
        PrShape4.setRotationPoint(-6F, -1F, 3F);
        PrShape4.setTextureSize(64, 32);
        PrShape4.mirror = true;
        setRotation(PrShape4, 0F, 0F, 0F);
        PlShape1 = new ModelRenderer(this, 0, 0);
        PlShape1.addBox(0F, 0F, 0F, 1, 6, 10);
        PlShape1.setRotationPoint(5F, -9F, -5F);
        PlShape1.setTextureSize(64, 32);
        PlShape1.mirror = true;
        setRotation(PlShape1, 0F, 0F, 0F);
        PlShape2 = new ModelRenderer(this, 0, 0);
        PlShape2.addBox(0F, 0F, 0F, 1, 1, 8);
        PlShape2.setRotationPoint(5F, -3F, -3F);
        PlShape2.setTextureSize(64, 32);
        PlShape2.mirror = true;
        setRotation(PlShape2, 0F, 0F, 0F);
        PlShape3 = new ModelRenderer(this, 0, 0);
        PlShape3.addBox(0F, 0F, 0F, 1, 1, 5);
        PlShape3.setRotationPoint(5F, -2F, 0F);
        PlShape3.setTextureSize(64, 32);
        PlShape3.mirror = true;
        setRotation(PlShape3, 0F, 0F, 0F);
        PlShape4 = new ModelRenderer(this, 0, 0);
        PlShape4.addBox(0F, 0F, 0F, 1, 1, 2);
        PlShape4.setRotationPoint(5F, -1F, 3F);
        PlShape4.setTextureSize(64, 32);
        PlShape4.mirror = true;
        setRotation(PlShape4, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float cnt, float v, float time, float yaw, float pitch, float scale) {
        GlStateManager.pushMatrix();
        if (entity.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }
        GlStateManager.pushMatrix();
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            GlStateManager.rotate(yaw, 0, 1, 0);
            GlStateManager.rotate(pitch, 1, 0, 0);
        }

        for (ModelRenderer model : models)
            model.render(scale);

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;

        if (!models.contains(model)) models.add(model);
    }

}
