package ru.leymooo.antirelog.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PvpTimeUpdateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final int oldTime;
    private int newTime;
    private final UpdateReason updateReason;
    private Player damagedPlayer;
    private Player damagedBy;
    private boolean cancelled;

    public PvpTimeUpdateEvent(Player player, int oldTime, int newTime) {
        this.player = player;
        this.oldTime = oldTime;
        this.newTime = newTime;
        this.updateReason = UpdateReason.TICK;
    }

    public PvpTimeUpdateEvent(Player player, int oldTime, int newTime, UpdateReason updateReason) {
        this.player = player;
        this.oldTime = oldTime;
        this.newTime = newTime;
        this.updateReason = updateReason;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOldTime() {
        return oldTime;
    }

    public int getNewTime() {
        return newTime;
    }

    public void setNewTime(int newTime) {
        if (newTime <= 0) {
            throw new IllegalArgumentException("New time must be positive");
        }
        this.newTime = newTime;
    }

    public UpdateReason getUpdateReason() {
        return updateReason;
    }

    public Player getDamagedPlayer() {
        return damagedPlayer;
    }

    public void setDamagedPlayer(Player damagedPlayer) {
        this.damagedPlayer = damagedPlayer;
    }

    public Player getDamagedBy() {
        return damagedBy;
    }

    public void setDamagedBy(Player damagedBy) {
        this.damagedBy = damagedBy;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum UpdateReason {
        TICK,
        DAMAGE_DEALT,
        DAMAGE_RECEIVED,
        API_CALL
    }
}