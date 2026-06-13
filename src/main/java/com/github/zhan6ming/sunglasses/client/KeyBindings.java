package com.github.zhan6ming.sunglasses.client;

import com.github.zhan6ming.sunglasses.network.ToggleNightVisionPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class KeyBindings {

    public static final KeyMapping TOGGLE_NIGHTVISION = new KeyMapping(
        "key.sunglasses.toggle_nightvision",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        "key.category.sunglasses"
    );

    public static final KeyMapping OPEN_OFFSET_CONFIG = new KeyMapping(
        "key.sunglasses.open_offset_config",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F10,
        "key.category.sunglasses"
    );

    private static boolean wasTogglePressed = false;
    private static boolean wasConfigPressed = false;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(KeyBindings::onClientTick);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean togglePressed = TOGGLE_NIGHTVISION.isDown();
        if (togglePressed && !wasTogglePressed) {
            PacketDistributor.sendToServer(new ToggleNightVisionPacket());
        }
        wasTogglePressed = togglePressed;

        boolean configPressed = OPEN_OFFSET_CONFIG.isDown();
        if (configPressed && !wasConfigPressed) {
            mc.setScreen(new OffsetConfigScreen(null,
                com.github.zhan6ming.sunglasses.event.PlayerOffsetData.getOffset(mc.player.getUUID())));
        }
        wasConfigPressed = configPressed;
    }
}
