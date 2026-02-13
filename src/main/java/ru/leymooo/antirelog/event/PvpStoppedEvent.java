package ru.leymooo.antirelog.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PvpStoppedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final StopReason reason;
    private final int timeWhenStopped;

    public PvpStoppedEvent(Player player) {
        this.player = player;
        this.reason = StopReason.API_CALL;
        this.timeWhenStopped = 0;
    }

    public PvpStoppedEvent(Player player, StopReason reason, int timeWhenStopped) {
        this.player = player;
        this.reason = reason;
        this.timeWhenStopped = timeWhenStopped;
    }

    public Player getPlayer() {
        return player;
    }

    public StopReason getReason() {
        return reason;
    }

    public int getTimeWhenStopped() {
        return timeWhenStopped;
    }

    public boolean isExpired() {
        return reason == StopReason.EXPIRED;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum StopReason {
        EXPIRED,
        DEATH,
        QUIT,
        KICK,
        ENTERED_IGNORED_REGION,
        PLUGIN_RELOAD,
        SERVER_SHUTDOWN,
        API_CALL
    }
}