package com.github.zhan6ming.sunglasses.event;

import com.github.zhan6ming.sunglasses.Sunglasses;
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

    private static final int EFFECT_DURATION = 999999;

    private static final Set<UUID> FEATURE_ENABLED = ConcurrentHashMap.newKeySet();

    public static void register() {
        NeoForge.EVENT_BUS.addListener(GogglesEventHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(GogglesEventHandler::onEquipmentChange);
    }

    /**
     * 切换「清晰视野」功能状态。
     * 如果功能已启用则禁用，反之亦然。此方法应在服务器端调用。
     *
     * @param player 要切换功能的玩家
     */
    public static void toggleFeature(Player player) {
        UUID uuid = player.getUUID();
        if (!FEATURE_ENABLED.add(uuid)) {
            FEATURE_ENABLED.remove(uuid);
            clearEffects(player);
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        UUID uuid = player.getUUID();
        boolean wearing = isWearingSunglasses(player);
        boolean featureOn = FEATURE_ENABLED.contains(uuid);

        if (wearing && featureOn) {
            // 施加自定义「清晰视野」效果（内部会自动施加原版夜视）
            MobEffectInstance current = player.getEffect(Sunglasses.CLEAR_VISION);
            if (current == null || current.getDuration() <= 200) {
                player.addEffect(new MobEffectInstance(
                    Sunglasses.CLEAR_VISION, EFFECT_DURATION, 0, false, false, true));
            }
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
     */
    private static boolean isWearingSunglasses(Player player) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SunglassesItem) {
            return true;
        }
        if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
            return com.github.zhan6ming.sunglasses.compat.curios.CuriosCompat.isWearingSunglasses(player);
        }
        return false;
    }

    private static void clearEffects(Player player) {
        // 移除自定义「清晰视野」效果
        player.removeEffect(Sunglasses.CLEAR_VISION);
        // 同步移除由 ClearVisionEffect 施加的原版夜视
        player.removeEffect(MobEffects.NIGHT_VISION);
    }
}
