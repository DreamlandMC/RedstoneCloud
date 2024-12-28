package de.redstonecloud.api.redis.broker;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import de.redstonecloud.api.redis.broker.packet.Packet;
import de.redstonecloud.api.redis.broker.packet.PacketRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public class Broker {
    public static final Gson GSON = new Gson();

    protected static Broker instance;

    public static Broker get() {
        return instance;
    }

    protected PacketRegistry packetRegistry;

    protected String mainRoute;
    protected Jedis subscriber;
    protected JedisPool pool;

    protected Object2ObjectOpenHashMap<String, ObjectArrayList<Consumer<Packet>>> consumers;
    protected Int2ObjectOpenHashMap<ResponseContainer<?>> pendingResponses;

    public Broker(String mainRoute, PacketRegistry packetRegistry, String... routes) {
        Preconditions.checkArgument(instance == null, "Broker already initialized");
        Preconditions.checkArgument(routes.length > 0, "Routes should not be empty");
        instance = this;

        this.mainRoute = mainRoute;

        this.packetRegistry = packetRegistry;

        this.consumers = new Object2ObjectOpenHashMap<>();
        this.pendingResponses = new Int2ObjectOpenHashMap<>();

        initJedis(routes);
    }

    private void initJedis(String... routes) {
        String address = System.getenv("REDIS_IP") != null ? System.getenv("REDIS_IP") : System.getProperty("redis.bind");
        int port = Integer.parseInt(System.getenv("REDIS_PORT") != null ? System.getenv("REDIS_PORT") : System.getProperty("redis.port"));

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(4);
        config.setMaxIdle(8);
        config.setMaxTotal(16);
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(true);
        config.setMaxWait(Duration.ofSeconds(1));
        config.setTestOnReturn(true);

        this.pool = new JedisPool(config, address, port);

        new Thread(() -> {
            try {
                this.subscriber = new Jedis(address, port, 0);
                this.subscriber.subscribe(new BrokerJedisPubSub(), routes);
            } catch (Exception ignored) {}
        }).start();
    }

    public void publish(Packet packet) {
        try (Jedis publisher = this.pool.getResource()) {
            publisher.publish(packet.getTo(), packet.finalDocument().toString());
        } catch (Exception ignored) {}
    }

    public void listen(String channel, Consumer<Packet> callback) {
        this.consumers.computeIfAbsent(channel, k -> new ObjectArrayList<>()).add(callback);
    }

    public void shutdown() {
        this.pool.close();
        this.subscriber.close();
    }

    public void addPendingResponse(int id, ResponseContainer<?> callback) {
        Preconditions.checkArgument(!this.pendingResponses.containsKey(id), "A message with the same id is already waiting for a response");
        this.pendingResponses.put(id, callback);
        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() ->
                Optional.ofNullable(this.pendingResponses.remove(id))
                        .ifPresent(responseContainer -> responseContainer.consumer().accept(null)));
    }

    @SuppressWarnings("unchecked")
    private class BrokerJedisPubSub extends JedisPubSub {
        @Override
        public void onMessage(String channel, String messageString) {
            JsonArray array = GSON.fromJson(messageString, JsonArray.class);
            Packet packet = packetRegistry.create(array);

            Optional.ofNullable(pendingResponses.remove(packet.getSessionId()))
                    .ifPresent(responseContainer -> {
                        Consumer<? extends Packet> consumer = responseContainer.consumer();
                        Class<? extends Packet> packetClass = responseContainer.packetClass();

                        if (packetClass.isInstance(packet))
                            ((Consumer<Packet>) consumer).accept(packetClass.cast(packet));
                    });

            consumers.getOrDefault(channel, new ObjectArrayList<>())
                    .forEach(consumer -> consumer.accept(packet));
        }
    }
}
