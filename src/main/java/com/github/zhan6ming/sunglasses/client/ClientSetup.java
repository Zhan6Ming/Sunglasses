package com.github.zhan6ming.sunglasses.client;

import com.github.zhan6ming.sunglasses.event.PlayerOffsetData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientSetup {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onRegisterKeyMappings);
        modEventBus.addListener(ClientSetup::onRegisterAdditional);
        modEventBus.addListener(ClientSetup::onModifyBakingResult);
        modEventBus.addListener(ClientSetup::onLayerRegister);
        NeoForge.EVENT_BUS.addListener(ClientSetup::onClientLogout);
    }

    /** 玩家退出时清除客户端偏移缓存，防止数据残留 */
    private static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PlayerOffsetData.clearClientCache();
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.TOGGLE_NIGHTVISION);
        event.register(KeyBindings.OPEN_OFFSET_CONFIG);
    }

    private static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        SunglassesModel.onRegisterAdditional(event);
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        SunglassesModel.onModifyBakingResult(event);
    }

    private static void onLayerRegister(EntityRenderersEvent.RegisterLayerDefinitions event) {
        if (ModList.get().isLoaded("curios")) {
            com.github.zhan6ming.sunglasses.compat.curios.CuriosRenderers.onLayerRegister(event);
        }
    }
}
