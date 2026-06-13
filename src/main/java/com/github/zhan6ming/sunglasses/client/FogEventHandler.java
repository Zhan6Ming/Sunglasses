package com.github.zhan6ming.sunglasses.client;

import com.github.zhan6ming.sunglasses.event.GogglesEventHandler;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;

@OnlyIn(Dist.CLIENT)
public class FogEventHandler {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, FogEventHandler::onRenderFog);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, FogEventHandler::onComputeFogColor);
    }

    /**
     * 获取当前处于流体中且拥有岩浆视野的玩家，若不满足条件则返回 null。
     */
    private static Player getGogglesPlayer() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return null;

        boolean inLava = player.isEyeInFluidType(NeoForgeMod.LAVA_TYPE.value());
        boolean inWater = player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value());

        if (!inLava && !inWater) return null;
        if (!GogglesEventHandler.hasLavaVision(player)) return null;

        return player;
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (getGogglesPlayer() == null) return;

        event.setFarPlaneDistance(20.0f);
        event.setNearPlaneDistance(0.0f);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (getGogglesPlayer() == null) return;

        event.setRed(0.0f);
        event.setGreen(0.0f);
        event.setBlue(0.0f);
    }
}
