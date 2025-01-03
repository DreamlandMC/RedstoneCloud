package de.redstonecloud.api.redis.broker;

import de.redstonecloud.api.redis.broker.packet.Packet;

import java.util.function.Consumer;

public record ResponseContainer<T extends Packet>(Class<T> packetClass, Consumer<T> consumer) {}
