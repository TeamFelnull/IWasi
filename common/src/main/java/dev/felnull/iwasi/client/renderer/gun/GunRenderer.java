package dev.felnull.iwasi.client.renderer.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.felnull.iwasi.client.data.DeltaGunItemTransData;
import dev.felnull.iwasi.client.data.DeltaGunPlayerTransData;
import dev.felnull.iwasi.client.data.InfoGunTrans;
import dev.felnull.iwasi.client.motion.gun.GunMotion;
import dev.felnull.iwasi.client.util.IWClientPlayerData;
import dev.felnull.iwasi.data.GunItemTransData;
import dev.felnull.iwasi.data.HoldType;
import dev.felnull.iwasi.data.IWPlayerData;
import dev.felnull.iwasi.gun.trans.item.GunItemTrans;
import dev.felnull.iwasi.gun.trans.player.AbstractReloadGunTrans;
import dev.felnull.iwasi.gun.trans.player.GunPlayerTrans;
import dev.felnull.iwasi.item.GunItem;
import dev.felnull.iwasi.util.IWItemUtil;
import dev.felnull.iwasi.util.IWPlayerUtil;
import dev.felnull.otyacraftengine.client.motion.MotionPose;
import dev.felnull.otyacraftengine.client.util.OEModelUtil;
import dev.felnull.otyacraftengine.client.util.OERenderUtil;
import dev.felnull.otyacraftengine.util.OEEntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public abstract class GunRenderer<M extends GunMotion> {
    protected static final Minecraft mc = Minecraft.getInstance();
    protected static final float SLIM_TRANS = 0.035f / 2f;

    abstract public void render(LivingEntity entity, ItemStack stack, ItemStack stackOld, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource multiBufferSource, float delta, int light, int overlay);

    public void renderHand(M motion, PoseStack poseStack, MultiBufferSource multiBufferSource, InteractionHand hand, int packedLight, float partialTicks, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack) {
        boolean off = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = off ? mc.player.getMainArm() : mc.player.getMainArm().getOpposite();
        boolean slim = OEModelUtil.isSlimPlayerModel(mc.player);
        boolean handFlg = arm == HumanoidArm.LEFT;
        float t = handFlg ? -1f : 1f;
        boolean bothHand = IWPlayerUtil.isBothHand(mc.player, hand);

        boolean hideOp = hand == InteractionHand.OFF_HAND || !bothHand;

        var cgtd = IWClientPlayerData.getGunTransData(mc.player, hand, partialTicks);

        // cgtd = new DeltaGunPlayerTransData(IWGunPlayerTrans.GLOCK_17_RELOAD, 1f, 1);
//IWGunTrans.GLOCK_17_RELOAD.getProgress(IWItemUtil.getGun(stack), 1)

        var cgt = cgtd.gunTrans();
        var igt = new InfoGunTrans(cgtd, stack);

        boolean tmpBothHand = bothHand;
        if (cgt != null && cgt.isUseBothHand()) tmpBothHand = true;

        float holdPar = IWPlayerUtil.getHoldProgress(mc.player, hand, partialTicks);
        poseStack.pushPose();
        OERenderUtil.poseHandItem(poseStack, arm, swingProgress, equipProgress);

        poseRecoil(motion, poseStack, arm, bothHand, partialTicks);

        poseStack.pushPose();

        poseHand(motion, poseStack, arm, bothHand, holdPar, igt);

        if (slim) poseStack.translate(t * -SLIM_TRANS, 0, 0);

        OERenderUtil.renderPlayerArmNoTransAndRot(poseStack, multiBufferSource, arm, packedLight);

        poseGun(motion, poseStack, arm, bothHand, holdPar, igt);
        if (slim) poseStack.translate(t * SLIM_TRANS, 0, 0);

        if (handFlg) poseStack.translate(1f, 0, 0f);
        OERenderUtil.renderHandItem(poseStack, multiBufferSource, arm, stack, packedLight);

        poseStack.popPose();

        if (bothHand || tmpBothHand) {
            poseStack.pushPose();
            var oparm = arm.getOpposite();
            poseOppositeHand(motion, poseStack, oparm, holdPar, igt, hideOp);

            if (slim) poseStack.translate(t * SLIM_TRANS, 0, 0);
            OERenderUtil.renderPlayerArmNoTransAndRot(poseStack, multiBufferSource, oparm, packedLight);

            poseStack.pushPose();
            ItemStack opItem = getOppositeItem(cgt, cgtd, OEEntityUtil.getOppositeHand(hand));
            //opItem = new ItemStack(IWItems.GLOCK_17_MAGAZINE.get());
            if (!opItem.isEmpty()) {
                poseOppositeItem(motion, poseStack, arm, holdPar, igt);
                // MotionDebug.getInstance().onDebug(poseStack, multiBufferSource, 0.5f);
                if (slim) poseStack.translate(t * -SLIM_TRANS, 0, 0);

                //if (handFlg)
                //    poseStack.translate(1f, 0, 0f);
                renderMagazine(opItem, poseStack, multiBufferSource, partialTicks, packedLight, OverlayTexture.NO_OVERLAY);
                // OERenderUtil.renderHandItem(poseStack, multiBufferSource, arm, opItem, packedLight);
            }
            poseStack.popPose();


            poseStack.pushPose();
            if (hand == InteractionHand.MAIN_HAND) {
                ItemStack offItem = mc.player.getItemInHand(OEEntityUtil.getOppositeHand(hand));
                if (IWItemUtil.isKnife(offItem)) {
                    poseOppositeKnife(motion, poseStack, arm, holdPar, igt);
                    OERenderUtil.renderHandItem(poseStack, multiBufferSource, arm.getOpposite(), offItem, packedLight);
                }
            }
            poseStack.popPose();


            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public void renderArmWithItem(M motion, ItemInHandLayer<? extends LivingEntity, ? extends EntityModel<?>> layer, LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, HumanoidArm arm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float delta) {
        if (!(livingEntity instanceof AbstractClientPlayer player)) return;
        boolean handFlg = arm == HumanoidArm.LEFT;
        boolean slim = OEModelUtil.isSlimPlayerModel(player);
        var cgtd = IWClientPlayerData.getGunTransData(player, OEEntityUtil.getHandByArm(livingEntity, arm), delta);
        var igt = new InfoGunTrans(cgtd, itemStack);
        boolean bothHand = IWPlayerUtil.isBothHand(player, OEEntityUtil.getHandByArm(player, arm));
        float t = handFlg ? -1f : 1f;
        float holdPar = IWPlayerUtil.getHoldProgress(player, OEEntityUtil.getHandByArm(player, arm), delta);

        poseStack.pushPose();
        layer.getParentModel().translateToHand(arm, poseStack);

        if (slim) poseStack.translate(t * -SLIM_TRANS, 0, 0);

        poseArmGun(motion, player, poseStack, arm, bothHand, igt, holdPar, delta);

        OERenderUtil.poseScaleAll(poseStack, 0.7f);
        if (handFlg) poseStack.translate(1f, 0, 0f);

        Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, transformType, handFlg, poseStack, multiBufferSource, i);
        poseStack.popPose();
    }

    public void poseOppositeArm(M motion, InteractionHand hand, HumanoidArm arm, HumanoidModel<? extends LivingEntity> model, ItemStack itemStack, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;
        float delta = OERenderUtil.getPartialTicks();
        boolean bothHand = IWPlayerUtil.isBothHand(player, OEEntityUtil.getOppositeHand(hand));
        var cgtd = IWClientPlayerData.getGunTransData(player, OEEntityUtil.getHandByArm(livingEntity, arm.getOpposite()), delta);
        var cgt = cgtd.gunTrans();
        boolean tmpBothHand = bothHand;
        if (cgt != null && cgt.isUseBothHand())
            tmpBothHand = true;

        if (bothHand || tmpBothHand) {
            var mainPart = arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
            var headPart = model.head;
            float holdPar = IWPlayerUtil.getHoldProgress(player, OEEntityUtil.getOppositeHand(hand), delta);
            var igt = new InfoGunTrans(cgtd, itemStack);
            var headPar = getHeadPar(player, holdPar, igt);
            float headParY = headPar.getRight();
            float headParX = headPar.getLeft();

            poseOffArm(motion, player, mainPart, headPart, arm, holdPar, headParY, headParX, igt, !bothHand);
            if (bothHand)
                poseArmRecoil(motion, player, mainPart, OEEntityUtil.getOppositeHand(hand), delta);
        }
    }

    public void poseArm(M motion, InteractionHand hand, HumanoidArm arm, HumanoidModel<? extends LivingEntity> model, ItemStack itemStack, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) return;
        boolean bothHand = IWPlayerUtil.isBothHand(player, hand);
        float delta = OERenderUtil.getPartialTicks();
        var headPart = model.head;
        var mainPart = arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
        var cgtd = IWClientPlayerData.getGunTransData(player, OEEntityUtil.getHandByArm(livingEntity, arm), delta);
        var igt = new InfoGunTrans(cgtd, itemStack);
        float holdPar = IWPlayerUtil.getHoldProgress(player, hand, delta);

        var headPar = getHeadPar(player, holdPar, igt);
        float headParY = headPar.getRight();
        float headParX = headPar.getLeft();

        poseMainArm(motion, player, mainPart, headPart, arm, bothHand, holdPar, headParY, headParX, igt);
        poseArmRecoil(motion, player, mainPart, hand, delta);
    }

    private Pair<Float, Float> getHeadPar(Player player, float holdPar, InfoGunTrans igt) {
        float headParX = 1f, headParY = 1f;
        var lh = IWPlayerData.getLastHold(player);

        if (lh.isDisarmament())
            headParY = 1f - holdPar;

        if (lh != HoldType.UPPER && lh.isDisarmament())
            headParX = 1f - holdPar;

        if (igt.gunTransData().gunTrans() != null) {
            if (igt.isLastStep()) {
                headParX *= igt.progressPar();
                headParY *= igt.progressPar();
            } else if (igt.gunTransData().step() == 0) {
                headParX *= 1f - igt.progressPar();
                headParY *= 1f - igt.progressPar();
            } else {
                headParX = headParY = 0;
            }
        }
        return Pair.of(headParX, headParY);
    }

    protected void poseArmRecoil(M motion, Player player, ModelPart armPart, InteractionHand recoilHand, float delta) {
        float rcp = IWPlayerData.getRecoil(player, recoilHand, delta);
        float b = -(float) Math.PI * 2f / 360f;
        armPart.xRot += b * 12f * rcp;
    }

    protected void poseMainArm(M motion, Player player, ModelPart armPart, ModelPart headPart, HumanoidArm arm, boolean bothHands, float hold, float headParY, float headParX, InfoGunTrans gunTrans) {
        MotionPose pose = motion.getArmPoseHoldMotion(arm, bothHands, IWPlayerData.getPreHold(player), IWPlayerData.getLastHold(player)).getPose(hold);

        if (gunTrans.gunTransData().gunTrans() instanceof AbstractReloadGunTrans) {
            pose = motion.getArmReloadMotion(arm, gunTrans, pose);
        }

        if (arm == HumanoidArm.LEFT)
            pose = pose.reverse();

        setArmByPose(armPart, headPart, pose, headParY, headParX);
    }

    protected void poseOffArm(M motion, Player player, ModelPart oppositeArmPart, ModelPart headPart, HumanoidArm arm, float hold, float headParY, float headParX, InfoGunTrans gunTrans, boolean tmpBothOnly) {
        MotionPose pose = motion.getOppositeArmPoseHoldMotion(arm, IWPlayerData.getPreHold(player), IWPlayerData.getLastHold(player)).getPose(hold);

        if (gunTrans.gunTransData().gunTrans() instanceof AbstractReloadGunTrans) {
            pose = motion.getOppositeArmReloadMotion(arm, gunTrans, pose);
        }

        if (arm == HumanoidArm.RIGHT)
            pose = pose.reverse();

        float par = 1f;
        if (tmpBothOnly && gunTrans.gunTransData().gunTrans() != null) {
            if (gunTrans.isLastStep()) {
                par *= 1f - gunTrans.progressPar();
            } else if (gunTrans.gunTransData().step() == 0) {
                par *= gunTrans.progressPar();
            }
        }

        setArmByPose(oppositeArmPart, headPart, pose, headParY, headParX, par);
    }

    private void setArmByPose(ModelPart part, ModelPart head, MotionPose pose, float headParY, float headParX) {
        setArmByPose(part, head, pose, headParY, headParX, 1f);
    }

    private void setArmByPose(ModelPart part, ModelPart head, MotionPose pose, float headParY, float headParX, float par) {
        float b = -(float) Math.PI * 2f / 360f;
        var ang = pose.rotation().angle();

        float x = b * ang.x() + head.xRot * headParY;

        float hy = head.yRot;
        hy %= ((float) Math.PI / 180f * 360f);
        if (hy >= 0) {
            if (hy > ((float) Math.PI / 180f * 360f) / 2f)
                hy = -((float) Math.PI / 180f * 360f) + hy;
        } else {
            if (hy < -((float) Math.PI / 180f * 360f) / 2f)
                hy = ((float) Math.PI / 180f * 360f) + hy;
        }

        float y = b * ang.y() + hy * headParX;
        float z = b * ang.z();

        part.xRot = Mth.lerp(par, part.xRot, x);
        part.yRot = Mth.lerp(par, part.yRot, y);
        part.zRot = Mth.lerp(par, part.zRot, z);
    }

    protected void poseArmGun(M motion, Player player, PoseStack poseStack, HumanoidArm arm, boolean bothHands, InfoGunTrans gunTrans, float holdPar, float delta) {
        var nh = IWPlayerData.getLastHold(player);
        var ph = IWPlayerData.getPreHold(player);

        if (gunTrans.gunTransData().gunTrans() != null) {
            nh = HoldType.NONE;
            ph = IWPlayerData.getLastHold(player);
            if (gunTrans.isLastStep()) {
                holdPar = 1f - gunTrans.progressPar();
            } else if (gunTrans.isFirstStep()) {
                holdPar = gunTrans.progressPar();
            } else {
                holdPar = 1f;
            }
        }

        var pose = motion.getArmGunPoseHoldMotion(arm, bothHands, ph, nh, player.getViewXRot(delta)).getPose(holdPar);

        if (arm == HumanoidArm.LEFT)
            pose = pose.reverse();
        pose.pose(poseStack);
    }

    protected ItemStack getOppositeItem(GunPlayerTrans gunPlayerTrans, DeltaGunPlayerTransData deltaGunPlayerTransData, InteractionHand hand) {
        ItemStack opItem = ItemStack.EMPTY;

        if (gunPlayerTrans instanceof AbstractReloadGunTrans && (deltaGunPlayerTransData.step() == 1 || deltaGunPlayerTransData.step() == 2)) {
            var lst = IWPlayerData.getTmpHandItems(mc.player, hand);
            if (!lst.isEmpty()) opItem = lst.get(0);
        }

        return opItem;
    }

    protected void poseRecoil(M motion, PoseStack stack, HumanoidArm arm, boolean bothHands, float delta) {
        float rcp = IWPlayerData.getRecoil(mc.player, OEEntityUtil.getHandByArm(mc.player, arm), delta);
        var ht = IWPlayerData.getLastHold(mc.player);
        var pose = motion.getRecoil(ht, rcp, bothHands);

        if (arm == HumanoidArm.LEFT) pose = pose.reverse();
        pose.pose(stack);
    }

    protected void poseHand(M motion, PoseStack stack, HumanoidArm arm, boolean bothHands, float hold, InfoGunTrans gunTrans) {
        MotionPose pose = motion.getHandHoldMotion(arm, bothHands, IWPlayerData.getPreHold(mc.player), IWPlayerData.getLastHold(mc.player)).getPose(hold);

        if (gunTrans.gunTransData().gunTrans() instanceof AbstractReloadGunTrans) {
            pose = motion.getHandReloadMotion(arm, gunTrans, pose);
        }

        if (arm == HumanoidArm.LEFT) pose = pose.reverse();
        pose.pose(stack);
    }

    protected void poseOppositeHand(M motion, PoseStack stack, HumanoidArm arm, float hold, InfoGunTrans gunTrans, boolean hide) {
        MotionPose pose = hide ? motion.getOppositeHandHideMotionPoint(arm).getPose() : motion.getOppositeHandHoldMotion(arm, IWPlayerData.getPreHold(mc.player), IWPlayerData.getLastHold(mc.player)).getPose(hold);

        if (gunTrans.gunTransData().gunTrans() instanceof AbstractReloadGunTrans) {
            pose = motion.getOppositeHandReloadMotion(arm, gunTrans, pose);
        }

        if (arm != HumanoidArm.LEFT) pose = pose.reverse();
        pose.pose(stack);
    }

    protected void poseGun(M motion, PoseStack stack, HumanoidArm arm, boolean bothHands, float hold, InfoGunTrans gunTrans) {
        MotionPose pose;
        var bp = motion.getGunFixedMotionPoint(arm, bothHands, IWPlayerData.getLastHold(mc.player));

        if (gunTrans.gunTransData().gunTrans() instanceof AbstractReloadGunTrans) {
            pose = bp.getPose();
        } else {
            pose = bp.getPose();
        }

        if (arm == HumanoidArm.LEFT) pose = pose.reverse();
        pose.pose(stack);
    }

    protected void poseOppositeItem(M motion, PoseStack stack, HumanoidArm arm, float hold, InfoGunTrans gunTrans) {
        MotionPose pose;
        var bp = motion.getOppositeItemFixedMotionPoint(arm, hold > 0.5);

        if (gunTrans.gunTransData().gunTrans() instanceof AbstractReloadGunTrans) {
            pose = bp.getPose();
        } else {
            pose = bp.getPose();
        }

        if (arm == HumanoidArm.LEFT) pose = pose.reverse();
        pose.pose(stack);
    }

    protected void poseOppositeKnife(M motion, PoseStack stack, HumanoidArm arm, float hold, InfoGunTrans gunTrans) {
        MotionPose pose;
        var bp = motion.getOppositeKnifeFixedMotionPoint(arm, hold > 0.5);

        pose = bp.getPose();

        if (arm == HumanoidArm.LEFT) pose = pose.reverse();
        pose.pose(stack);
    }

    protected DeltaGunItemTransData getGunItemTrans(GunItemTrans gunItemTrans, ItemStack stack, ItemStack oldStack, float delta) {
        if (stack.isEmpty() || oldStack.isEmpty()) return null;
        var ng = GunItem.getGunItemTrans(stack, gunItemTrans.getName());
        var og = GunItem.getGunItemTrans(oldStack, gunItemTrans.getName());
        if (ng == null && og == null) return null;
        if (ng == null) ng = new GunItemTransData(null);
        if (og == null) og = new GunItemTransData(null);

        return DeltaGunItemTransData.of(delta, og, ng, stack);
    }

    abstract void renderMagazine(ItemStack stack, PoseStack poseStack, MultiBufferSource ms, float delta, int light, int overlay);
}
