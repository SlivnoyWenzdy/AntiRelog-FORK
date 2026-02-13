package ru.leymooo.antirelog.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.leymooo.antirelog.manager.CooldownManager.CooldownType;

public class PvpCooldownEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final CooldownType cooldownType;
    private final Action action;
    private final long duration;
    private boolean cancelled;

    public PvpCooldownEvent(Player player, CooldownType cooldownType, Action action, long duration) {
        this.player = player;
        this.cooldownType = cooldownType;
        this.action = action;
        this.duration = duration;
    }

    public Player getPlayer() {
        return player;
    }

    public CooldownType getCooldownType() {
        return cooldownType;
    }

    public Action getAction() {
        return action;
    }

    public long getDuration() {
        return duration;
    }

    public long getDurationSeconds() {
        return duration / 1000;
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

    public enum Action {
        ITEM_USED,
        ITEM_BLOCKED,
        COOLDOWN_ACTIVE
    }
}