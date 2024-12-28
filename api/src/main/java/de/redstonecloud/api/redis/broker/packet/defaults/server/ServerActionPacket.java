package de.redstonecloud.api.redis.broker.packet.defaults.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
public class ServerActionPacket extends Packet {
    public static String NETWORK_ID = "9";
    
    protected String action;
    protected String playerUuid;
    protected JsonObject extraData;

    @Override
    public String packetId() {
        return NETWORK_ID;
    }

    @Override
    public void serialize(JsonArray data) {
        data.add(this.playerUuid);
        data.add(this.action);
        data.add(this.extraData);
    }

    @Override
    public void deserialize(JsonArray data) {
        this.playerUuid = data.get(0).getAsString();
        this.action = data.get(1).getAsString();
        this.extraData = data.get(2).getAsJsonObject();
    }
}
