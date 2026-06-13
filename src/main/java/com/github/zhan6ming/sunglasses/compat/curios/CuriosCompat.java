package com.github.zhan6ming.sunglasses.compat.curios;

import com.github.zhan6ming.sunglasses.item.SunglassesItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

public class CuriosCompat {

    public static void init(IEventBus modEventBus) {
        // Create 联动：仅在 Create 存在时注册护目镜谓词
        if (net.neoforged.fml.ModList.get().isLoaded("create")) {
            try {
                // 使用全限定名引用 Create API，避免类加载时因 Create 未安装而崩溃
                com.simibubi.create.content.equipment.goggles.GogglesItem.addIsWearingPredicate(CuriosCompat::isWearingSunglasses);
            } catch (Exception e) {
                com.mojang.logging.LogUtils.getLogger().warn("Sunglasses: Create Curios compat failed: {}", e.getMessage());
            }
        }
        modEventBus.addListener(CuriosCompat::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        CuriosRenderers.register();
    }

    /**
     * 解析玩家的 Curios 物品栏能力，返回 curiosMap。
     *
     * @param entity 要检查的实体
     * @return Curios 物品映射的 Optional 包装
     */
    public static Optional<Map<String, ICurioStacksHandler>> resolveCuriosMap(LivingEntity entity) {
        return Optional.ofNullable(entity.getCapability(CuriosCapability.INVENTORY))
            .map(ICuriosItemHandler::getCurios);
    }

    /**
     * 检查玩家是否穿戴了 Sunglasses（通过 Curios 槽位）。
     * 此方法同时用于 Create 的 GogglesItem 谓词和 GogglesEventHandler 的穿戴检测。
     *
     * @param player 要检查的玩家
     * @return 如果在 Curios 中找到 Sunglasses 返回 true
     */
    public static boolean isWearingSunglasses(Player player) {
        return resolveCuriosMap(player)
            .map(CuriosCompat::hasSunglassesCurio)
            .orElse(false);
    }

    /**
     * 遍历 Curios 物品映射，检查是否存在 Sunglasses。
     *
     * @param curiosMap Curios 物品映射
     * @return 如果找到 Sunglasses 返回 true
     */
    public static boolean hasSunglassesCurio(Map<String, ICurioStacksHandler> curiosMap) {
        if (curiosMap == null) return false;

        for (ICurioStacksHandler stacksHandler : curiosMap.values()) {
            int slots = stacksHandler.getSlots();
            for (int slot = 0; slot < slots; slot++) {
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(slot);
                if (stack.getItem() instanceof SunglassesItem) {
                    return true;
                }
            }
        }
        return false;
    }
}
