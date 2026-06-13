package com.github.zhan6ming.sunglasses.compat.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@OnlyIn(Dist.CLIENT)
public class SunglassesCurioRenderer implements ICurioRenderer {

    private final HumanoidModel<LivingEntity> model;

    public SunglassesCurioRenderer(ModelPart headPart) {
        this.model = new HumanoidModel<>(headPart);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource bufferSource,
            int packedLight, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        model.setupAnim(slotContext.entity(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.prepareMobModel(slotContext.entity(), limbSwing, limbSwingAmount, partialTick);
        ICurioRenderer.followHeadRotations(slotContext.entity(), model.head);

        poseStack.pushPose();

        // 16.0: Minecraft 模型坐标到世界坐标的转换因子（1 block = 16 model units）
        poseStack.translate(model.head.x / 16.0, model.head.y / 16.0, model.head.z / 16.0);
        // 应用头部旋转
        poseStack.mulPose(Axis.ZP.rotation(model.head.zRot));
        poseStack.mulPose(Axis.YP.rotation(model.head.yRot));
        poseStack.mulPose(Axis.XP.rotation(model.head.xRot));

        // -0.25: 向下微调护目镜位置
        poseStack.translate(0, -0.25, 0);
        // 180.0f: 修正模型朝向（翻转 Z 轴）
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        // 0.625f: 缩放比（10/16），适配头部尺寸
        poseStack.scale(0.625f, 0.625f, 0.625f);

        // 偏移由 SunglassesModel.applyTransform(HEAD) 统一处理，这里不再重复应用

        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer().renderStatic(
            stack, ItemDisplayContext.HEAD, packedLight, OverlayTexture.NO_OVERLAY,
            poseStack, bufferSource, mc.level, 0
        );

        poseStack.popPose();
    }
}
