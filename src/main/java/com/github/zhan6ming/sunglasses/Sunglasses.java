package com.github.zhan6ming.sunglasses;

import com.github.zhan6ming.sunglasses.item.SunglassesItem;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(Sunglasses.MODID)
public class Sunglasses {

    public static final String MODID = "sunglasses";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    /**
     * 玩家墨镜 Y 轴偏移的持久化附件。
     * 使用 Codec 序列化，服务器重启后数据不丢失；copyOnDeath 确保死亡后保留。
     */
    public static final Supplier<AttachmentType<Double>> OFFSET_ATTACHMENT =
        ATTACHMENT_TYPES.register(
            "offset",
            () -> AttachmentType.builder(() -> 0.0)
                .serialize(com.mojang.serialization.Codec.DOUBLE)
                .copyOnDeath()
                .build()
        );

    public static final DeferredItem<SunglassesItem> SUNGLASSES = ITEMS.register(
        "sunglasses",
        () -> new SunglassesItem(new Item.Properties())
    );

    public static final DeferredItem<Item> ROUGH_LENS = ITEMS.registerSimpleItem("rough_lens");

    public static final DeferredItem<Item> ULTRA_LENS = ITEMS.registerSimpleItem("ultra_lens");

    @SuppressWarnings("unused") // 注册副作用：类加载时触发 DeferredRegister 注册
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SUNGLASSES_TAB =
        CREATIVE_MODE_TABS.register("sunglasses_tab", () ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.sunglasses"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(() -> SUNGLASSES.get().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    output.accept(SUNGLASSES.get());
                    output.accept(ROUGH_LENS.get());
                    output.accept(ULTRA_LENS.get());
                })
                .build()
        );

    public Sunglasses(IEventBus modEventBus, @SuppressWarnings("unused") ModContainer modContainer) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        com.github.zhan6ming.sunglasses.client.ClientSetup.register(modEventBus);
        com.github.zhan6ming.sunglasses.network.NetworkHandler.register(modEventBus);

        // GAME bus 事件处理器注册
        com.github.zhan6ming.sunglasses.event.GogglesEventHandler.register();
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            com.github.zhan6ming.sunglasses.client.FogEventHandler.register();
            com.github.zhan6ming.sunglasses.client.KeyBindings.register();
        }

        if (ModList.get().isLoaded("curios")) {
            try {
                com.github.zhan6ming.sunglasses.compat.curios.CuriosCompat.init(modEventBus);
                LOGGER.debug("Curios compat enabled");
            } catch (Exception e) {
                LOGGER.warn("Curios compat failed: {}", e.getMessage());
            }
        }

        LOGGER.info("Sunglasses loaded!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Create 联动：仅在 Create 存在时注册护目镜谓词（检查 HEAD 装备槽）
        // 注意：Curios 兼容层（CuriosCompat）会额外注册一个检查 Curios 槽位的谓词
        // 两个谓词互补（OR 关系），确保无论穿戴方式如何都能被 Create 识别
        if (ModList.get().isLoaded("create")) {
            try {
                com.simibubi.create.content.equipment.goggles.GogglesItem.addIsWearingPredicate(player ->
                    player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SunglassesItem
                );
                LOGGER.debug("Sunglasses: Create compat - HEAD slot goggle predicate registered");
            } catch (Exception e) {
                LOGGER.warn("Sunglasses: Create compat failed: {}", e.getMessage());
            }
        }
        LOGGER.debug("Sunglasses common setup complete");
    }
}
