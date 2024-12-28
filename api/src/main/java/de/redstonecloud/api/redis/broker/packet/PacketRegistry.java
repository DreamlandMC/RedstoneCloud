package de.redstonecloud.api.redis.broker.packet;

import com.google.gson.JsonArray;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.function.Supplier;

public class PacketRegistry {

    protected final Object2ObjectOpenHashMap<String, Supplier<? extends Packet>> registry = new Object2ObjectOpenHashMap<>();

    public void register(String type, Supplier<? extends Packet> supplier) {
        this.registry.put(type, supplier);
    }

    public Packet create(String type) {
        Supplier<? extends Packet> supplier = registry.get(type);
        if (supplier == null) {
            throw new IllegalArgumentException("Invalid packet type " + type);
        }

        return supplier.get();
    }

    public Packet create(JsonArray packetData) {
        Packet packet = this.create(packetData.get(0).getAsString());

        packet.setSessionId(packetData.get(1).getAsInt());
        packet.setFrom(packetData.get(2).getAsString());
        packet.setTo(packetData.get(3).getAsString());
        packet.deserialize(packetData.get(4).getAsJsonArray());

        return packet;
    }
}
