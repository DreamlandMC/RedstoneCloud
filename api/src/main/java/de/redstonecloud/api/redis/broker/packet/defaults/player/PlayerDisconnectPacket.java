package de.redstonecloud.api.redis.broker.packet.defaults.player;

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
public class PlayerDisconnectPacket extends Packet {
    public static int NETWORK_ID = 6;

    protected String uuid;
    protected String server;

    @Override
    public int packetId() {
        return NETWORK_ID;
    }

    @Override
    public void serialize(JsonArray data) {
        data.add(this.uuid);
        data.add(this.server);
    }

    @Override
    public void deserialize(JsonArray data) {
        this.uuid = data.get(0).getAsString();
        this.server = data.get(1).getAsString();
    }
}
