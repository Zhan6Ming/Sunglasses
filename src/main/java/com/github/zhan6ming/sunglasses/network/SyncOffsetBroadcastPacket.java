package com.github.zhan6ming.sunglasses.network;

import com.github.zhan6ming.sunglasses.Sunglasses;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SyncOffsetBroadcastPacket(UUID playerUUID, double offsetY) implements CustomPacketPayload {

    public static final Type<SyncOffsetBroadcastPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Sunglasses.MODID, "sync_offset_broadcast"));

    public static final StreamCodec<FriendlyByteBuf, SyncOffsetBroadcastPacket> CODEC = new StreamCodec<>() {
        @Override
        public SyncOffsetBroadcastPacket decode(FriendlyByteBuf buf) {
            return new SyncOffsetBroadcastPacket(buf.readUUID(), buf.readDouble());
        }

        @Override
        public void encode(FriendlyByteBuf buf, SyncOffsetBroadcastPacket packet) {
            buf.writeUUID(packet.playerUUID);
            buf.writeDouble(packet.offsetY);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
