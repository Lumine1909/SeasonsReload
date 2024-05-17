package io.github.lumine1909.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PacketOutEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private Object packet;
    private final Player player;

    public PacketOutEvent(@NotNull Object packet, @NotNull Player player) {
        super(true);
        this.packet = packet;
        this.player = player;
    }

    public Object getPacket() {
        return packet;
    }

    public void setPacket(Object packet) {
        this.packet = packet;
    }

    public Player getPlayer() {
        return player;
    }
}