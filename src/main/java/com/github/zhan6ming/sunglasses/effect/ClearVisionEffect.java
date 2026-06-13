package com.github.zhan6ming.sunglasses.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 清晰视野效果 —— 合并了夜视与液体中清晰视野两个功能。
 * <p>
 * 效果激活时：
 * 1. 为实体施加原版夜视效果（提供地下/夜间照明）
 * 2. FogEventHandler 检测到此效果后，解除液体中的迷雾限制
 */
public class ClearVisionEffect extends MobEffect {

    private static final int NIGHT_VISION_REFRESH = 200; // 剩余 ≤10秒 时刷新

    public ClearVisionEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 每次 tick 刷新原版夜视效果，保持持续生效
        if (!entity.level().isClientSide()) {
            MobEffectInstance current = entity.getEffect(MobEffects.NIGHT_VISION);
            if (current == null || current.getDuration() <= NIGHT_VISION_REFRESH) {
                entity.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION, 400, 0, false, false, false
                ));
            }
        }
        return true; // 保持效果不被移除
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true; // 每 tick 都执行
    }
}
