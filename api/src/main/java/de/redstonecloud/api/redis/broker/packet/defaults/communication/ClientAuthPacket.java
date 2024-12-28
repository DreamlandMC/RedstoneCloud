package de.redstonecloud.api.redis.broker.packet.defaults.communication;

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
public class ClientAuthPacket extends Packet {
    public static String NETWORK_ID = "0";

    protected String clientId;

    @Override
    public String packetId() {
        return NETWORK_ID;
    }

    @Override
    public void serialize(JsonArray data) {
        data.add(this.clientId);
    }

    @Override
    public void deserialize(JsonArray data) {
        this.clientId = data.get(0).getAsString();
    }
}
