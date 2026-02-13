package ru.leymooo.antirelog.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PvpPlayerKilledEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final KillReason killReason;
    private final int timeWhenKilled;

    public PvpPlayerKilledEvent(Player player, KillReason killReason, int timeWhenKilled) {
        this.player = player;
        this.killReason = killReason;
        this.timeWhenKilled = timeWhenKilled;
    }

    public Player getPlayer() {
        return player;
    }

    public KillReason getKillReason() {
        return killReason;
    }

    public int getTimeWhenKilled() {
        return timeWhenKilled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum KillReason {
        QUIT_IN_PVP,
        KICKED_IN_PVP,
        SERVER_SHUTDOWN
    }
}