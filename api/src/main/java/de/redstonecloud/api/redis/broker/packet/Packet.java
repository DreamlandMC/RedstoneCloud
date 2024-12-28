package de.redstonecloud.api.redis.broker.packet;

import com.google.gson.JsonArray;
import de.redstonecloud.api.redis.broker.Broker;
import de.redstonecloud.api.redis.broker.ResponseContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Getter
@Setter
@Accessors(chain = true)
public abstract class Packet {
    public abstract String packetId();
    public abstract void serialize(JsonArray data);
    public abstract void deserialize(JsonArray data);

    protected int sessionId = ThreadLocalRandom.current().nextInt(0, 1000);

    protected String from = Broker.get().getMainRoute();
    protected String to;

    public JsonArray finalDocument() {
        JsonArray array = new JsonArray();
        array.add(this.packetId());
        array.add(this.sessionId);
        array.add(this.from);
        array.add(this.to);

        JsonArray serialized = new JsonArray();
        this.serialize(serialized);
        array.add(serialized);

        return array;
    }

    public void send() {
        this.send(null, null);
    }

    public <T extends Packet> void send(Class<T> packetType, Consumer<T> callback) {
        Broker broker = Broker.get();

        if (callback != null)
            broker.addPendingResponse(this.sessionId, new ResponseContainer<>(packetType, callback));

        broker.publish(this);
    }
}
