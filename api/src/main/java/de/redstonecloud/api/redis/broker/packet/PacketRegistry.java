package de.redstonecloud.api.redis.broker.packet;

import com.google.gson.JsonArray;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.function.Supplier;

public class PacketRegistry {

    protected final Int2ObjectOpenHashMap<Supplier<? extends Packet>> registry = new Int2ObjectOpenHashMap<>();

    public void register(int type, Supplier<? extends Packet> supplier) {
        this.registry.put(type, supplier);
    }

    public Packet create(int type) {
        Supplier<? extends Packet> supplier = registry.get(type);
        if (supplier == null) {
            return null;
        }

        return supplier.get();
    }

    public Packet create(JsonArray packetData) {
        Packet packet = this.create(packetData.get(1).getAsInt());

        if (packet != null) {
            packet.setSessionId(packetData.get(2).getAsInt());
            packet.setFrom(packetData.get(3).getAsString());
            packet.setTo(packetData.get(4).getAsString());
            packet.deserialize(packetData.get(5).getAsJsonArray());
        }

        return packet;
    }
}
