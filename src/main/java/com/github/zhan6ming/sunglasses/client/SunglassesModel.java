package com.github.zhan6ming.sunglasses.client;

import com.github.zhan6ming.sunglasses.Sunglasses;
import com.github.zhan6ming.sunglasses.event.PlayerOffsetData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

@OnlyIn(Dist.CLIENT)
public class SunglassesModel extends BakedModelWrapper<BakedModel> {

    public static final ResourceLocation SUNGLASSES_3D_RL =
        ResourceLocation.fromNamespaceAndPath(Sunglasses.MODID, "item/sunglasses_model");

    public static final ModelResourceLocation SUNGLASSES_3D_MRL =
        ModelResourceLocation.standalone(SUNGLASSES_3D_RL);

    private static BakedModel cachedModel3D;
    private static boolean initialized = false;

    public SunglassesModel(BakedModel template) {
        super(template);
    }

    /**
     * 在 HEAD 显示上下文中应用自定义模型和偏移变换。
     * <p>
     * 执行逻辑：
     * 1. 仅在 HEAD 上下文时替换为 3D 模型
     * 2. 从 {@link PlayerOffsetData} 读取当前玩家的 Y 轴偏移值
     * 3. 将偏移应用到 PoseStack，允许玩家自定义护目镜竖直位置
     *
     * @param displayContext 物品显示上下文
     * @param poseStack      位置堆栈，用于变换
     * @param leftHanded     是否使用左手
     * @return 变换后的模型（HEAD 上下文时为 3D 模型，否则为原始模型）
     */
    @Override
    @javax.annotation.Nonnull
    public BakedModel applyTransform(@javax.annotation.Nonnull ItemDisplayContext displayContext, @javax.annotation.Nonnull PoseStack poseStack, boolean leftHanded) {
        if (displayContext == ItemDisplayContext.HEAD) {
            BakedModel headModel = getModel3D();
            if (headModel != null) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    double offsetY = PlayerOffsetData.getOffset(mc.player.getUUID());
                    if (offsetY != 0.0) {
                        // 应用 Y 轴偏移，允许玩家自定义护目镜的竖直位置
                        poseStack.translate(0, offsetY, 0);
                    }
                }
                return headModel.applyTransform(displayContext, poseStack, leftHanded);
            }
        }
        return super.applyTransform(displayContext, poseStack, leftHanded);
    }

    private static BakedModel getModel3D() {
        if (!initialized) {
            cachedModel3D = Minecraft.getInstance().getModelManager().getModel(SUNGLASSES_3D_MRL);
            initialized = true;
        }
        return cachedModel3D;
    }

    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        event.register(SUNGLASSES_3D_MRL);
    }

    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        cachedModel3D = null;
        initialized = false;

        ResourceLocation itemRL = ResourceLocation.fromNamespaceAndPath(Sunglasses.MODID, "sunglasses");
        ModelResourceLocation itemMRL = ModelResourceLocation.inventory(itemRL);

        BakedModel originalModel = event.getModels().get(itemMRL);
        if (originalModel != null) {
            event.getModels().put(itemMRL, new SunglassesModel(originalModel));
        }
    }
}
