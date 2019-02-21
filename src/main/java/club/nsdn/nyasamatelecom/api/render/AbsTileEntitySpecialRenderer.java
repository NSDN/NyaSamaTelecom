package club.nsdn.nyasamatelecom.api.render;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tool.ToolBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

/**
 * Created by drzzm32 on 2019.1.29.
 */
public abstract class AbsTileEntitySpecialRenderer extends TileEntitySpecialRenderer<TileEntity> {

    @Override
    public void render(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
        if (te instanceof TileEntityBase) {
            render((TileEntityBase) te, x, y, z, partialTicks, destroyStage, partial);
            if (Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ToolBase) {
                BlockPos pos = te.getPos();
                BlockPos playerPos = Minecraft.getMinecraft().player.getPosition();
                Vec3d vec = new Vec3d(pos.subtract(playerPos));
                if (vec.lengthVector() > Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 4)
                    return;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x - pos.getX(), y - pos.getY(), z - pos.getZ());
                RendererHelper.renderConnection((TileEntityBase) te);
                GlStateManager.popMatrix();
            }
        }
    }

    public abstract void render(
            @Nonnull TileEntityBase te,
            double x, double y, double z,
            float partialTicks, int destroyStage, float partial
    );

}
