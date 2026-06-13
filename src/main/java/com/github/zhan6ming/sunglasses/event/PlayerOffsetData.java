package com.github.zhan6ming.sunglasses.event;

import com.github.zhan6ming.sunglasses.Sunglasses;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家墨镜 Y 轴偏移数据管理。
 * <p>
 * 服务端：通过 {@link Sunglasses#OFFSET_ATTACHMENT} (AttachmentType) 持久化存储，
 * 服务器重启后数据不丢失。
 * <p>
 * 客户端：使用内存缓存 {@link #CLIENT_OFFSETS}，由网络包同步更新。
 */
public class PlayerOffsetData {

    /** 客户端偏移缓存，由 SyncOffsetBroadcastPacket 填充 */
    private static final Map<UUID, Double> CLIENT_OFFSETS = new ConcurrentHashMap<>();

    /**
     * 设置玩家偏移（服务端）。
     * 将数据写入 AttachmentType，同时标记需要保存。
     */
    public static void setOffset(Player player, double offset) {
        player.setData(Sunglasses.OFFSET_ATTACHMENT, offset);
    }

    /**
     * 获取玩家偏移（通用，自动判断客户端/服务端）。
     * 服务端从 AttachmentType 读取，客户端从缓存读取。
     */
    public static double getOffset(Player player) {
        if (player.level().isClientSide()) {
            return CLIENT_OFFSETS.getOrDefault(player.getUUID(), 0.0);
        }
        return player.getData(Sunglasses.OFFSET_ATTACHMENT);
    }

    /**
     * 通过 UUID 获取偏移（仅客户端使用）。
     * 用于渲染时无法直接获取 Player 对象的场景。
     */
    public static double getOffset(UUID uuid) {
        return CLIENT_OFFSETS.getOrDefault(uuid, 0.0);
    }

    /**
     * 设置客户端缓存偏移（由网络包调用）。
     */
    public static void setClientOffset(UUID uuid, double offset) {
        CLIENT_OFFSETS.put(uuid, offset);
    }

    /**
     * 清除客户端缓存（玩家退出时调用）。
     */
    public static void clearClientCache() {
        CLIENT_OFFSETS.clear();
    }
}
