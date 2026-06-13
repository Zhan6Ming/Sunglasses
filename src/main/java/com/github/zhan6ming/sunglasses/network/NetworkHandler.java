package com.github.zhan6ming.sunglasses.network;

import com.github.zhan6ming.sunglasses.Sunglasses;
import com.github.zhan6ming.sunglasses.event.PlayerOffsetData;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::onRegisterPayloads);
        NeoForge.EVENT_BUS.addListener(NetworkHandler::onPlayerLoggedIn);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Sunglasses.MODID).versioned("1");

        registrar.playToServer(
            ToggleNightVisionPacket.TYPE,
            ToggleNightVisionPacket.CODEC,
            NetworkHandler::handleToggleNightVision
        );

        registrar.playToServer(
            SyncOffsetPacket.TYPE,
            SyncOffsetPacket.CODEC,
            NetworkHandler::handleSyncOffsetFromClient
        );

        registrar.playToClient(
            SyncOffsetBroadcastPacket.TYPE,
            SyncOffsetBroadcastPacket.CODEC,
            NetworkHandler::handleSyncOffsetBroadcast
        );
    }

    /**
     * 玩家登录时，向该玩家广播所有在线玩家的偏移数据。
     * 确保新加入的玩家能正确看到其他玩家的墨镜位置。
     */
    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer joining = (ServerPlayer) event.getEntity();
        // 先将新玩家自己的持久化偏移广播给所有人
        double ownOffset = joining.getData(Sunglasses.OFFSET_ATTACHMENT);
        if (ownOffset != 0.0) {
            PacketDistributor.sendToAllPlayers(
                new SyncOffsetBroadcastPacket(joining.getUUID(), ownOffset)
            );
        }
        // 再将所有在线玩家的偏移发送给新玩家
        for (ServerPlayer online : joining.server.getPlayerList().getPlayers()) {
            if (online.equals(joining)) continue;
            double offset = online.getData(Sunglasses.OFFSET_ATTACHMENT);
            if (offset != 0.0) {
                PacketDistributor.sendToPlayer(joining, new SyncOffsetBroadcastPacket(online.getUUID(), offset));
            }
        }
    }

    private static void handleToggleNightVision(ToggleNightVisionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                packet.handle((ServerPlayer) context.player());
            }
        }).exceptionally(e -> {
            LOGGER.error("Failed to handle toggle night vision packet", e);
            return null;
        });
    }

    private static void handleSyncOffsetFromClient(SyncOffsetPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                // 持久化存储到 AttachmentType
                PlayerOffsetData.setOffset(sp, packet.offsetY());
                // 广播给所有在线玩家
                PacketDistributor.sendToAllPlayers(
                    new SyncOffsetBroadcastPacket(sp.getUUID(), packet.offsetY())
                );
            }
        }).exceptionally(e -> {
            LOGGER.error("Failed to handle sync offset packet from client", e);
            return null;
        });
    }

    private static void handleSyncOffsetBroadcast(SyncOffsetBroadcastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 客户端：更新本地缓存
            PlayerOffsetData.setClientOffset(packet.playerUUID(), packet.offsetY());
        }).exceptionally(e -> {
            LOGGER.error("Failed to handle sync offset broadcast", e);
            return null;
        });
    }
}
