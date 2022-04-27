package dev.felnull.iwasi.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.EventResult;
import dev.felnull.iwasi.client.renderer.item.GunItemRenderer;
import dev.felnull.iwasi.item.GunItem;
import dev.felnull.otyacraftengine.client.event.MoreRenderEvent;
import dev.felnull.otyacraftengine.client.util.OERenderUtil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RenderHandler {
    public static void init() {
        MoreRenderEvent.RENDER_ITEM_IN_HAND.register(RenderHandler::onRenderItemInHand);
        MoreRenderEvent.RENDER_ARM_WITH_ITEM.register(RenderHandler::onRenderArmWithItem);
    }

    public static EventResult onRenderItemInHand(PoseStack poseStack, MultiBufferSource multiBufferSource, InteractionHand hand, int packedLight, float partialTicks, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack) {
        if (stack.getItem() instanceof GunItem gunItem) {
            var gir = GunItemRenderer.GUN_ITEM_RENDERERS.get(gunItem.getGun());
            if (gir != null)
                gir.renderHand(poseStack, multiBufferSource, hand, packedLight, partialTicks, interpolatedPitch, swingProgress, equipProgress, stack);
            return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }

    public static EventResult onRenderArmWithItem(ItemInHandLayer<? extends LivingEntity, ? extends EntityModel<?>> itemInHandLayer, LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        if (itemStack.getItem() instanceof GunItem gunItem) {
            var gir = GunItemRenderer.GUN_ITEM_RENDERERS.get(gunItem.getGun());
            if (gir != null)
                gir.renderArmWithItem(itemInHandLayer, livingEntity, itemStack, transformType, humanoidArm, poseStack, multiBufferSource, i, OERenderUtil.getPartialTicks());
            return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }
}
