package com.github.zhan6ming.sunglasses.event;

import com.github.zhan6ming.sunglasses.item.SunglassesItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GogglesEventHandler {

    private static final int NIGHT_VISION_DURATION = 999999;

    private static final Set<UUID> FEATURE_ENABLED = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> HAS_NIGHT_VISION = ConcurrentHashMap.newKeySet();
    public static final Set<UUID> HAS_LAVA_VISION = ConcurrentHashMap.newKeySet();

    public static void register() {
        NeoForge.EVENT_BUS.addListener(GogglesEventHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(GogglesEventHandler::onEquipmentChange);
    }

    /**
     * 切换护目镜的夜视功能状态。
     * 如果功能已启用则禁用，反之亦然。此方法应在服务器端调用。
     *
     * @param player 要切换功能的玩家
     */
    public static void toggleFeature(Player player) {
        UUID uuid = player.getUUID();
        // 使用 add/remove 的返回值实现原子性切换，避免竞态条件
        if (!FEATURE_ENABLED.add(uuid)) {
            // add 返回 false → 已存在 → 移除并清除效果
            FEATURE_ENABLED.remove(uuid);
            clearEffects(player);
        }
        // add 返回 true → 之前不存在 → 已添加，等待下个 tick 自动施加效果
    }

    /**
     * 获取玩家是否拥有岩浆/水中的特殊视野能力。
     * 只有穿戴 Sunglasses 且启用了功能的玩家才会返回 true。
     *
     * @param player 要检查的玩家
     * @return 玩家是否有岩浆视野
     */
    public static boolean hasLavaVision(Player player) {
        return HAS_LAVA_VISION.contains(player.getUUID());
    }

    /** 剩余持续时间低于此值时才重新施加效果，避免每 tick 创建新对象 */
    private static final int REFRESH_THRESHOLD = 200; // 10 秒

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        UUID uuid = player.getUUID();
        boolean wearing = isWearingSunglasses(player);
        boolean featureOn = FEATURE_ENABLED.contains(uuid);

        if (wearing && featureOn) {
            MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
            if (current == null || current.getDuration() <= REFRESH_THRESHOLD) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, false, false, true));
            }
            HAS_NIGHT_VISION.add(uuid);
            HAS_LAVA_VISION.add(uuid);
        } else {
            clearEffects(player);
        }
    }

    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getSlot() != EquipmentSlot.HEAD) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        if (from.getItem() instanceof SunglassesItem && !(to.getItem() instanceof SunglassesItem)) {
            clearEffects(player);
            FEATURE_ENABLED.remove(player.getUUID());
        }
    }

    /**
     * 检查玩家是否穿戴了 Sunglasses（头部装备或 Curios 槽位）。
     * Curios 检查委托给 CuriosCompat 以确保类加载安全。
     */
    private static boolean isWearingSunglasses(Player player) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SunglassesItem) {
            return true;
        }
        if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
            // 委托给 CuriosCompat，避免在 Curios 未加载时直接引用 Curios API 类
            return com.github.zhan6ming.sunglasses.compat.curios.CuriosCompat.isWearingSunglasses(player);
        }
        return false;
    }

    private static void clearEffects(Player player) {
        UUID uuid = player.getUUID();
        if (HAS_NIGHT_VISION.remove(uuid)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
        HAS_LAVA_VISION.remove(uuid);
    }
}
