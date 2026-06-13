package com.github.zhan6ming.sunglasses.network;

import com.github.zhan6ming.sunglasses.Sunglasses;
import com.github.zhan6ming.sunglasses.event.GogglesEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ToggleNightVisionPacket() implements CustomPacketPayload {

    public static final Type<ToggleNightVisionPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Sunglasses.MODID, "toggle_nightvision"));

    public static final StreamCodec<FriendlyByteBuf, ToggleNightVisionPacket> CODEC =
        StreamCodec.unit(new ToggleNightVisionPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        GogglesEventHandler.toggleFeature(player);
    }
}
