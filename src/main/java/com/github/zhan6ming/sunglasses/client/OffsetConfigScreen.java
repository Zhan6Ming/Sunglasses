package com.github.zhan6ming.sunglasses.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import com.github.zhan6ming.sunglasses.network.SyncOffsetPacket;

@OnlyIn(Dist.CLIENT)
public class OffsetConfigScreen extends Screen {

    private static final double MIN_OFFSET = -0.20;
    private static final double MAX_OFFSET = 0.50;

    private final Screen parent;
    private double currentOffset;
    private double lastSentOffset;
    private OffsetSlider slider;

    public OffsetConfigScreen(Screen parent, double currentOffset) {
        super(Component.translatable("screen.sunglasses.offset_config"));
        this.parent = parent;
        this.currentOffset = currentOffset;
        this.lastSentOffset = currentOffset;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        slider = new OffsetSlider(centerX - 100, centerY - 20, 200, 20, currentOffset);
        this.addRenderableWidget(slider);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
            .bounds(centerX - 100, centerY + 10, 200, 20).build());
    }

    @Override
    public void render(@javax.annotation.Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 45, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
            Component.translatable("screen.sunglasses.offset_config.current", String.format("%.2f", slider.getValue())),
            this.width / 2, this.height / 2 + 40, 0xAAAAAA);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        // 仅在关闭时且值有变化时发送网络包
        if (currentOffset != lastSentOffset && this.minecraft != null && this.minecraft.player != null) {
            PacketDistributor.sendToServer(new SyncOffsetPacket(this.minecraft.player.getUUID(), currentOffset));
            lastSentOffset = currentOffset;
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private class OffsetSlider extends AbstractSliderButton {
        OffsetSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, CommonComponents.EMPTY, (value - MIN_OFFSET) / (MAX_OFFSET - MIN_OFFSET));
            updateMessage();
        }

        double getValue() {
            return MIN_OFFSET + this.value * (MAX_OFFSET - MIN_OFFSET);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("screen.sunglasses.offset_config.slider", String.format("%.2f", getValue())));
        }

        @Override
        protected void applyValue() {
            currentOffset = getValue();
        }
    }
}
