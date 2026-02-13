package ru.leymooo.antirelog.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.Set;

public class PowerUpsDisabledEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Player attackedBy;
    private final Set<PowerUpType> disabledPowerUps;

    public PowerUpsDisabledEvent(Player player, Player attackedBy, Set<PowerUpType> disabledPowerUps) {
        this.player = player;
        this.attackedBy = attackedBy;
        this.disabledPowerUps = Collections.unmodifiableSet(disabledPowerUps);
    }

    public Player getPlayer() {
        return player;
    }

    public Player getAttackedBy() {
        return attackedBy;
    }

    public Set<PowerUpType> getDisabledPowerUps() {
        return disabledPowerUps;
    }

    public boolean wasDisabled(PowerUpType type) {
        return disabledPowerUps.contains(type);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum PowerUpType {
        CREATIVE,
        FLY,
        ESSENTIALS_VANISH,
        ESSENTIALS_GOD,
        CMI_GOD,
        CMI_VANISH,
        SUPERVANISH,
        VANISH_NO_PACKET,
        LIBS_DISGUISES
    }
}