package ru.leymooo.antirelog.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.leymooo.antirelog.event.PvpPreStartEvent.PvPStatus;

public class PvpStartedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player defender;
    private final Player attacker;
    private final int pvpTime;
    private final PvPStatus pvpStatus;
    private final long startTimestamp;

    public PvpStartedEvent(Player defender, Player attacker, int pvpTime, PvPStatus pvpStatus) {
        this.defender = defender;
        this.attacker = attacker;
        this.pvpTime = pvpTime;
        this.pvpStatus = pvpStatus;
        this.startTimestamp = System.currentTimeMillis();
    }

    public Player getDefender() {
        return defender;
    }

    public Player getAttacker() {
        return attacker;
    }

    public int getPvpTime() {
        return pvpTime;
    }

    public PvPStatus getPvpStatus() {
        return pvpStatus;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}