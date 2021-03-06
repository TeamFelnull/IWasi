package dev.felnull.iwasi.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.felnull.fnjl.math.FNVec3d;
import dev.felnull.iwasi.entity.bullet.Bullet;
import dev.felnull.otyacraftengine.client.util.OEModelUtil;
import dev.felnull.otyacraftengine.client.util.OERenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

// https://github.com/TeamFelnull/IWasi/blob/b9c4e77f37fe20e341ca84a7a3c8d114ebddac78/common/src/main/java/dev/felnull/iwasi/client/renderer/entity/TestBulletRenderer.java
public class BulletRenderer extends EntityRenderer<Bullet> {
    private static final FNVec3d ZERO = new FNVec3d(0, 0, 0);

    protected BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull Bullet entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        var vec = entity.getDeltaMovement();


        float xzd = (float) Math.sqrt(Math.pow(vec.x(), 2) + Math.pow(vec.z(), 2));
        float ya = (float) Math.toDegrees(Math.acos(vec.x() / xzd));
        if (!Float.isFinite(ya))
            ya = 0;

        if (vec.z() > 0)
            ya = -ya;

        float cyd = (float) Math.sqrt(Math.pow(xzd, 2) + Math.pow(vec.y(), 2));
        float ra = (float) Math.toDegrees(Math.acos(xzd / cyd));
        if (vec.y() > 0)
            ra = -ra;

        poseStack.pushPose();
        OERenderUtil.poseTrans16(poseStack, -0.5f, 0, -1f);
        OERenderUtil.poseTrans16(poseStack, 0.5f, 0.5f, 1f);

        OERenderUtil.poseRotateY(poseStack, ya + 90f);
        OERenderUtil.poseRotateX(poseStack, ra);

        OERenderUtil.poseTrans16(poseStack, -0.5f, -0.5f, -1f);

        var model = OEModelUtil.getModel(Blocks.DIAMOND_BLOCK.defaultBlockState());//OEModelUtil.getModel(IWModels.BULLET);
        var vc = multiBufferSource.getBuffer(Sheets.cutoutBlockSheet());
       // OERenderUtil.renderModel(poseStack, vc, model, i, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        super.render(entity, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull Bullet entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
