package club.nsdn.nyasamatelecom.renderer;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import club.nsdn.nyasamatelecom.TelecomItems;
import club.nsdn.nyasamatelecom.item.NyaGameMR;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.Arrays;

/**
 * The NyaGame MR helmet model and its heads-up display, ported from the 1.12.2 {@code RenderNyaGameMR}.
 *
 * <h2>The 1.12.2 class did two unrelated jobs</h2>
 * <ol>
 *   <li>it <b>was</b> the helmet model (a {@code ModelBiped} subclass holding nineteen hand-placed boxes);
 *       the item returned it from {@code getArmorModel(...)};</li>
 *   <li>it <b>drew the HUD</b> -- a text printer over the 128 glyph plates in {@code textures/fonts/}, plus
 *       a 128x64 backing plate -- driven from {@code ItemArmor#renderHelmetOverlay(...)}.</li>
 * </ol>
 * Both are ported, but neither can be expressed the 1.12.2 way, and the reasons are recorded per part
 * below rather than hidden.</p>
 *
 * <h2>The helmet model</h2>
 * <p>{@code ModelBiped} is {@link HumanoidModel} and a model is no longer a bag of {@code ModelRenderer}
 * objects created in a constructor: it is a {@link LayerDefinition} (a {@code MeshDefinition} of named
 * {@link PartDefinition}s) baked into a {@link ModelPart} tree, registered through
 * {@code EntityRenderersEvent.RegisterLayerDefinitions}, and attached to the item through
 * {@code IClientItemExtensions#getHumanoidArmorModel}. Verified with {@code javap} against
 * {@code neoforge-21.1.256-merged.jar}: {@code HumanoidModel.createMesh}, {@code PartDefinition#getChild},
 * {@code PartDefinition#addOrReplaceChild}, {@code CubeListBuilder#texOffs/#mirror/#addBox},
 * {@code PartPose#offset}, {@code LayerDefinition#create} and {@code EntityModelSet#bakeLayer} all exist
 * with the shapes used here.</p>
 *
 * <p><b>One deliberate structural change:</b> 1.12.2 kept the nineteen boxes in a flat list and rendered
 * them after applying {@code rotate(yaw)} / {@code rotate(pitch)} about the model origin. Here they are
 * children of {@code head}, with the same texture offsets and the same {@code setRotationPoint} values used
 * as {@code PartPose.offset}. {@code HumanoidModel} already rotates {@code head} by the head yaw and pitch,
 * so "boxes that follow the head" is the same visible result; the only difference is that the pivot is the
 * head's instead of the body's, which is what every modern armor model does.</p>
 *
 * <h2>The HUD</h2>
 * <p>{@code renderHelmetOverlay} does not exist in 1.21.1. The equivalent is a GUI layer registered through
 * {@code RegisterGuiLayersEvent}, which is what {@link #registerGuiLayer} does. That also forces one API
 * change: the text buffer and the printer can no longer live on a singleton instance of this class, because
 * an instance now needs a baked {@link ModelPart} that does not exist at class-initialisation time. The
 * buffer and {@link #clear()}/{@link #print}/{@link #drawHUDBack}/{@link #drawHUDString} are therefore
 * <b>static</b>, which is the one signature difference from 1.12.2 (they were instance members of
 * {@code INSTANCE}).</p>
 *
 * <p>The per-character drawing keeps the 1.12.2 scheme -- one glyph plate per character, tinted with
 * {@code GL11.glColor3f} then, {@code GuiGraphics#setColor} now (both verified to exist) -- and
 * {@code MinecraftForge.MC_VERSION} becomes {@code SharedConstants.getCurrentVersion().getName()}.</p>
 *
 * <p><b>NOT PORTED, with reasons:</b> three HUD lines had no verifiable successor and were dropped rather
 * than guessed at: {@code Tick: %1.2f ms} ({@code Minecraft#getTickLength} is gone),
 * {@code Render: %1.2f ms} ({@code Minecraft#getRenderPartialTicks} is gone; its replacement is the
 * {@code DeltaTracker} handed to the layer, which measures something else) and the {@code [%08X]} line,
 * which printed a hex digest of the partial-tick argument that {@code renderHelmetOverlay} used to receive
 * and that no longer reaches a GUI layer. Everything else -- the username, the mod banner, the motion,
 * speed and vertical-velocity lines -- is transcribed with the same format strings and the same unit
 * conversions ({@code * 20 * 3.6} for km/h, {@code * 20} for m/s).</p>
 */
public class RenderNyaGameMR extends HumanoidModel<LivingEntity> {

    /** The layer the helmet model is baked from; registered in {@link #registerLayerDefinitions}. */
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NyaSamaTelecom.MODID, "nyagame_mr"), "main");

    /** 1.12.2 {@code getArmorTexture}: {@code nyasamatelecom:textures/item/nyagame_mr.png}. */
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NyaSamaTelecom.MODID, "textures/item/nyagame_mr.png");

    private static final ResourceLocation HUD_BACK =
            ResourceLocation.fromNamespaceAndPath(NyaSamaTelecom.MODID, "textures/item/nyagame_mr_hud.png");

    /** Id of the GUI layer; see {@link #registerGuiLayer}. */
    public static final ResourceLocation HUD_LAYER =
            ResourceLocation.fromNamespaceAndPath(NyaSamaTelecom.MODID, "nyagame_mr_hud");

    /** 1.12.2 field widths, kept verbatim: the printer is a 29x13 character grid of 4x4 plates. */
    public static final int WIDTH = 29, HEIGH = 13, OFFSET = 6, SIZE = 4;

    private static String[] buffer = new String[HEIGH];
    private static int ptr = 0;

    public RenderNyaGameMR(ModelPart root) {
        super(root);
    }

    /**
     * The nineteen 1.12.2 {@code ModelRenderer} boxes, re-expressed as parts of the head.
     *
     * <p>Every box keeps its 1.12.2 texture offset, its {@code addBox} extents and its
     * {@code setRotationPoint} (as {@code PartPose.offset}); the {@code mirror = true} flag every one of them
     * carried is kept with {@code CubeListBuilder#mirror()}; the texture sheet is the old 64x32; and every
     * {@code setRotation(model, 0F, 0F, 0F)} asked for no rotation, so no rotation is written.</p>
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition head = mesh.getRoot().getChild("head");

        // GlassL / GlassR: the two visor panes.
        add(head, "glass_l", 32, 16, 1F, -5F, -6F, 2, 1, 1);
        add(head, "glass_r", 32, 18, -3F, -5F, -6F, 2, 1, 1);
        // Shape1..Shape7: the frame.
        add(head, "shape1", 0, 0, -5F, -10F, -5F, 10, 1, 10);
        add(head, "shape2", 0, 0, -5F, -9F, -6F, 2, 6, 1);
        add(head, "shape3", 0, 0, 3F, -9F, -6F, 2, 6, 1);
        add(head, "shape4", 0, 0, -1F, -5F, -6F, 2, 1, 1);
        add(head, "shape5", 0, 0, -3F, -4F, -6F, 6, 1, 1);
        add(head, "shape6", 0, 0, -3F, -9F, -6F, 6, 4, 1);
        add(head, "shape7", 0, 0, -5F, -9F, 5F, 10, 9, 1);
        // Back plate.
        add(head, "back", 0, 16, -4.5F, -9F, 6.1F, 9, 9, 0);
        // PrShape1..4 / PlShape1..4: the right and left rims.
        add(head, "pr_shape1", 0, 0, -6F, -9F, -5F, 1, 6, 10);
        add(head, "pr_shape2", 0, 0, -6F, -3F, -3F, 1, 1, 8);
        add(head, "pr_shape3", 0, 0, -6F, -2F, 0F, 1, 1, 5);
        add(head, "pr_shape4", 0, 0, -6F, -1F, 3F, 1, 1, 2);
        add(head, "pl_shape1", 0, 0, 5F, -9F, -5F, 1, 6, 10);
        add(head, "pl_shape2", 0, 0, 5F, -3F, -3F, 1, 1, 8);
        add(head, "pl_shape3", 0, 0, 5F, -2F, 0F, 1, 1, 5);
        add(head, "pl_shape4", 0, 0, 5F, -1F, 3F, 1, 1, 2);

        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void add(PartDefinition parent, String name, int u, int v,
                            float x, float y, float z, int w, int h, int d) {
        parent.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v).mirror().addBox(0F, 0F, 0F, w, h, d),
                PartPose.offset(x, y, z));
    }

    /** 1.12.2 {@code TileEntityModelBinder}'s {@code ClientRegistry.bindTileEntitySpecialRenderer} era had no
     * equivalent need for a layer; this is the modern half of {@code getArmorModel(...)}. */
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LAYER, RenderNyaGameMR::createBodyLayer);
    }

    /**
     * 1.12.2 {@code NyaGameMR#getArmorModel(...)}: hand this helmet model to the armor renderer.
     *
     * <p>In 1.21.1 that hook is a client item extension; {@code RegisterClientExtensionsEvent} is a mod-bus
     * event (javap-verified) and {@code IClientItemExtensions#getHumanoidArmorModel(LivingEntity, ItemStack,
     * EquipmentSlot, HumanoidModel)} is the method the armor layer calls (javap-verified). The model is baked
     * on demand from {@link #LAYER}.</p>
     */
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (!(stack.getItem() instanceof NyaGameMR)) {
                    return original;
                }
                return new RenderNyaGameMR(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
            }
        }, TelecomItems.NYAGAME_MR.get());
    }

    /**
     * 1.12.2 {@code ItemArmor#renderHelmetOverlay}: the HUD.
     *
     * <p>Registered above everything else, because that is what a helmet overlay is -- the 1.12.2 call was
     * made by the armor item at the end of the helmet render pass.</p>
     */
    public static void registerGuiLayer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(HUD_LAYER, (guiGraphics, deltaTracker) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }
            drawHelmetOverlay(guiGraphics, minecraft.player);
        });
    }

    /* -------- the text printer (1.12.2 instance members of INSTANCE, static here; see the class note) -------- */

    public static void clear() {
        buffer = new String[HEIGH];
        ptr = 0;
    }

    public static void print(String format, Object... args) {
        if (ptr >= HEIGH) {
            ptr = HEIGH - 1;
            buffer = Arrays.copyOfRange(buffer, 1, HEIGH + 1);
        }
        buffer[ptr] = String.format(format, args);
        ptr += 1;
    }

    /** 1.12.2 {@code drawHUDBack}: the 128x64 backing plate at the top-left, drawn with the whole texture. */
    public static void drawHUDBack(GuiGraphics guiGraphics) {
        guiGraphics.blit(HUD_BACK, 0, 0, 0, 0, 128, 64, 128, 64);
    }

    public static void drawHUDString(GuiGraphics guiGraphics) {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == null) buffer[i] = "";
            renderString(
                    buffer[i].substring(0, Math.min(buffer[i].length(), WIDTH)),
                    OFFSET, OFFSET + i * SIZE, 0x84FFFF,
                    guiGraphics
            );
        }
    }

    private static void renderString(String str, int x, int y, int color, GuiGraphics guiGraphics) {
        for (int i = 0; i < str.length(); i++) {
            renderChar(x + i * SIZE, y, str.charAt(i), color, guiGraphics);
        }
    }

    /** 1.12.2 {@code drawTexturedColoredModalRect}: a SIZE x SIZE plate, tinted, alpha 192. */
    private static void renderChar(int x, int y, char c, int color, GuiGraphics guiGraphics) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = 192 / 255.0F;
        guiGraphics.setColor(r, g, b, a);
        guiGraphics.blit(DeviceModels.font(c & 0xFF), x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * The 1.12.2 {@code renderHelmetOverlay} body, minus the three lines listed as {@code NOT PORTED} in the
     * class javadoc. Only draws while the player actually wears the helmet, which is what made 1.12.2 route
     * it through the item in the first place.
     */
    public static void drawHelmetOverlay(GuiGraphics guiGraphics, Player player) {
        if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof NyaGameMR)) {
            return;
        }

        drawHUDBack(guiGraphics);

        clear();
        print("NyaGame MR (TM) by NSDN (R)");
        print("  ");
        print("Hello, %s", Minecraft.getInstance().getUser().getName());
        print("  ");
        print("Ver:    %s", SharedConstants.getCurrentVersion().getName());
        double vx = player.getX() - player.xo;
        double vy = player.getY() - player.yo;
        double vz = player.getZ() - player.zo;
        double v = Math.sqrt(vx * vx + vy * vy + vz * vz);
        print("  ");
        print("Motion: %1.2f m/tick", v);
        print("Speed:  %1.2f km/h", v * 20 * 3.6);
        print("V-Vel:  %1.2f m/s", vy * 20);
        print("  ");

        drawHUDString(guiGraphics);
    }
}
