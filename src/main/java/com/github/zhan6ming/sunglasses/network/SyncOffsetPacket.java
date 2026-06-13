package com.github.zhan6ming.sunglasses.network;

import com.github.zhan6ming.sunglasses.Sunglasses;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SyncOffsetPacket(UUID playerUUID, double offsetY) implements CustomPacketPayload {

    public static final Type<SyncOffsetPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Sunglasses.MODID, "sync_offset"));

    public static final StreamCodec<FriendlyByteBuf, SyncOffsetPacket> CODEC = new StreamCodec<>() {
        @Override
        public SyncOffsetPacket decode(FriendlyByteBuf buf) {
            return new SyncOffsetPacket(buf.readUUID(), buf.readDouble());
        }

        @Override
        public void encode(FriendlyByteBuf buf, SyncOffsetPacket packet) {
            buf.writeUUID(packet.playerUUID);
            buf.writeDouble(packet.offsetY);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
