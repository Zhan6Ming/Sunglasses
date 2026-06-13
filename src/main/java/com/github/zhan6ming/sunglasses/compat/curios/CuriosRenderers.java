package com.github.zhan6ming.sunglasses.compat.curios;

import com.github.zhan6ming.sunglasses.Sunglasses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@OnlyIn(Dist.CLIENT)
public class CuriosRenderers {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Sunglasses.MODID, "sunglasses"), "main"
    );

    public static void register() {
        CuriosRendererRegistry.register(
            Sunglasses.SUNGLASSES.get(),
            () -> new SunglassesCurioRenderer(
                Minecraft.getInstance().getEntityModels().bakeLayer(LAYER)
            )
        );
    }

    public static void onLayerRegister(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LAYER, () ->
            LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 1, 1)
        );
    }
}
