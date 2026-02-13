package ru.leymooo.antirelog.manager;

import org.bukkit.entity.Player;
import ru.leymooo.antirelog.config.Settings;
import ru.leymooo.antirelog.data.PlayerData;

import java.util.HashMap;
import java.util.Map;

public class BossbarManager {

    private final Settings settings;
    private final Map<Player, PlayerData> playerDataMap = new HashMap<>();
    private boolean enabled = true;

    public BossbarManager(Settings settings) {
        this.settings = settings;
    }

    public void createBossBars() {
    }

    public PlayerData createPlayerData(Player player, int time, boolean silent) {
        removePlayerData(player);
        PlayerData data = new PlayerData(settings, time, player, silent);
        if (!enabled) {
            data.setBossBarVisible(false);
        }
        playerDataMap.put(player, data);
        return data;
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player);
    }

    public void updateBossBar(Player player) {
        PlayerData data = playerDataMap.get(player);
        if (data != null) {
            data.updateBossBar(settings);
        }
    }

    public void setBossBar(Player player, int time) {
        PlayerData data = playerDataMap.get(player);
        if (data != null && !data.isSilent() && enabled) {
            data.setDelay(time);
            data.updateBossBar(settings);
        }
    }

    public void clearBossbar(Player player) {
        removePlayerData(player);
    }

    public void clearBossbars() {
        for (PlayerData data : playerDataMap.values()) {
            data.removeBossBar();
        }
        playerDataMap.clear();
    }

    public void removePlayerData(Player player) {
        PlayerData data = playerDataMap.remove(player);
        if (data != null) {
            data.removeBossBar();
        }
    }

    public void setVisible(Player player, boolean visible) {
        PlayerData data = playerDataMap.get(player);
        if (data != null) {
            data.setBossBarVisible(visible);
        }
    }

    public boolean isVisible(Player player) {
        PlayerData data = playerDataMap.get(player);
        if (data == null) {
            return true;
        }
        return data.isBossBarVisible();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            for (PlayerData data : playerDataMap.values()) {
                data.setBossBarVisible(false);
            }
        } else {
            for (PlayerData data : playerDataMap.values()) {
                data.setBossBarVisible(true);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasPlayerData(Player player) {
        return playerDataMap.containsKey(player);
    }
}