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
public class ServerChangeStatusPacket extends Packet {
    public static int NETWORK_ID = 10;

    protected String server;
    protected String newStatus;

    @Override
    public int packetId() {
        return NETWORK_ID;
    }

    @Override
    public void serialize(JsonArray data) {
        data.add(this.server);
        data.add(this.newStatus);
    }

    @Override
    public void deserialize(JsonArray data) {
        this.server = data.get(0).getAsString();
        this.newStatus = data.get(1).getAsString();
    }
}
