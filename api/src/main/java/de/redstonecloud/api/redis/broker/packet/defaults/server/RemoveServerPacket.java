package de.redstonecloud.api.redis.broker.packet.defaults.server;

import com.google.gson.JsonArray;
import de.redstonecloud.api.redis.broker.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class RemoveServerPacket extends Packet {
    public static String NETWORK_ID = "7";

    protected String server;

    @Override
    public String packetId() {
        return NETWORK_ID;
    }

    @Override
    public void serialize(JsonArray data) {
        data.add(this.server);
    }

    @Override
    public void deserialize(JsonArray data) {
        this.server = data.get(0).getAsString();
    }
}
