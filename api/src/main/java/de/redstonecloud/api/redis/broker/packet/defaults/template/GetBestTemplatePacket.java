package de.redstonecloud.api.redis.broker.packet.defaults.template;

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
public class GetBestTemplatePacket extends Packet {
    public static String NETWORK_ID = "1";

    protected String template;

    @Override
    public String packetId() {
        return NETWORK_ID;
    }

    @Override
    public void serialize(JsonArray data) {
        data.add(this.template);
    }

    @Override
    public void deserialize(JsonArray data) {
        this.template = data.get(0).getAsString();
    }
}
