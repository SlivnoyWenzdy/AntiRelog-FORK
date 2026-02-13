package ru.leymooo.antirelog.api;

import org.bukkit.entity.Player;
import ru.leymooo.antirelog.manager.CooldownManager.CooldownType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class PvPSnapshot {

    private final UUID playerUuid;
    private final String playerName;
    private final boolean inPvP;
    private final boolean inSilentPvP;
    private final boolean infinitePvP;
    private final boolean bypassed;
    private final int timeRemaining;
    private final int customPvPDuration;
    private final boolean bossbarVisible;
    private final long timestamp;
    private final Map<CooldownType, Long> cooldowns;

    PvPSnapshot(Player player, AntiRelogAPI api) {
        this.playerUuid = player.getUniqueId();
        this.playerName = player.getName();
        this.inPvP = api.isInPvP(player);
        this.inSilentPvP = api.isInAnyPvP(player) && !this.inPvP;
        this.infinitePvP = api.isInfinitePvP(player);
        this.bypassed = api.isBypassed(player);
        this.timeRemaining = api.getTimeRemaining(player);
        this.customPvPDuration = api.getPlayerPvPDuration(player);
        this.bossbarVisible = api.isBossbarVisible(player);
        this.timestamp = System.currentTimeMillis();
        EnumMap<CooldownType, Long> map = new EnumMap<>(CooldownType.class);
        for (CooldownType type : CooldownType.values) {
            if (api.hasInfiniteCooldown(player, type)) {
                map.put(type, AntiRelogAPI.INFINITE_COOLDOWN);
            } else {
                long remaining = api.getCooldownRemaining(player, type);
                if (remaining > 0) {
                    map.put(type, remaining);
                }
            }
        }
        this.cooldowns = Collections.unmodifiableMap(map);
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isInPvP() {
        return inPvP;
    }

    public boolean isInSilentPvP() {
        return inSilentPvP;
    }

    public boolean isInfinitePvP() {
        return infinitePvP;
    }

    public boolean isBypassed() {
        return bypassed;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public int getCustomPvPDuration() {
        return customPvPDuration;
    }

    public boolean isBossbarVisible() {
        return bossbarVisible;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<CooldownType, Long> getCooldowns() {
        return cooldowns;
    }

    public boolean hasCooldown(CooldownType type) {
        return cooldowns.containsKey(type);
    }

    public long getCooldownRemaining(CooldownType type) {
        return cooldowns.getOrDefault(type, 0L);
    }

    public boolean hasInfiniteCooldown(CooldownType type) {
        Long value = cooldowns.get(type);
        return value != null && value == AntiRelogAPI.INFINITE_COOLDOWN;
    }

    @Override
    public String toString() {
        return "PvPSnapshot{" +
                "player=" + playerName +
                ", inPvP=" + inPvP +
                ", inSilentPvP=" + inSilentPvP +
                ", infinitePvP=" + infinitePvP +
                ", bypassed=" + bypassed +
                ", timeRemaining=" + timeRemaining +
                ", customPvPDuration=" + customPvPDuration +
                ", bossbarVisible=" + bossbarVisible +
                ", cooldowns=" + cooldowns.size() +
                '}';
    }
}