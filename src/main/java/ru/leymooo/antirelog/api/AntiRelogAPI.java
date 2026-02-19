package ru.leymooo.antirelog.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.leymooo.antirelog.Antirelog;
import ru.leymooo.antirelog.data.PlayerData;
import ru.leymooo.antirelog.manager.BossbarManager;
import ru.leymooo.antirelog.manager.CooldownManager;
import ru.leymooo.antirelog.manager.CooldownManager.CooldownType;
import ru.leymooo.antirelog.manager.PvPManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class AntiRelogAPI {

    public static final int INFINITE_TIME = -1;
    public static final long INFINITE_COOLDOWN = -1L;

    private static AntiRelogAPI instance;

    private final Antirelog plugin;
    private final PvPManager pvpManager;
    private final CooldownManager cooldownManager;
    private final BossbarManager bossbarManager;

    private AntiRelogAPI(Antirelog plugin) {
        this.plugin = plugin;
        this.pvpManager = plugin.getPvpManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.bossbarManager = plugin.getBossbarManager();
    }

    public static AntiRelogAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AntiRelog is not enabled");
        }
        return instance;
    }

    public static void init(Antirelog plugin) {
        instance = new AntiRelogAPI(plugin);
    }

    public static void disable() {
        instance = null;
    }

    public static boolean isAvailable() {
        return instance != null;
    }

    public boolean isInPvP(Player player) {
        return pvpManager.isInPvP(player);
    }

    public boolean isInPvP(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && pvpManager.isInPvP(player);
    }

    public boolean isInAnyPvP(Player player) {
        return pvpManager.isInPvP(player) || pvpManager.isInSilentPvP(player);
    }

    public int getTimeRemaining(Player player) {
        return pvpManager.getTimeRemainingInPvP(player);
    }

    public int getTimeRemaining(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? pvpManager.getTimeRemainingInPvP(player) : 0;
    }

    public boolean isInfinitePvP(Player player) {
        return pvpManager.isInfinitePvP(player);
    }

    public int getDefaultPvPDuration() {
        return plugin.getSettings().getPvpTime();
    }

    public int getPlayerPvPDuration(Player player) {
        return pvpManager.getPlayerPvPTime(player);
    }

    public void setPlayerPvPDuration(Player player, int time) {
        pvpManager.setPlayerPvPTime(player, time);
    }

    public void setInfinitePvP(Player player) {
        pvpManager.setPlayerPvPTime(player, INFINITE_TIME);
    }

    public void removePlayerPvPDuration(Player player) {
        pvpManager.removePlayerPvPTime(player);
    }

    public boolean hasCustomPvPDuration(Player player) {
        return pvpManager.hasCustomPvPTime(player);
    }

    public void forcePvP(Player attacker, Player defender) {
        pvpManager.playerDamagedByPlayer(attacker, defender);
    }

    public void forcePvP(Player attacker, Player defender, int customTime) {
        pvpManager.setPlayerPvPTime(attacker, customTime);
        pvpManager.setPlayerPvPTime(defender, customTime);
        pvpManager.playerDamagedByPlayer(attacker, defender);
    }

    public void forceInfinitePvP(Player attacker, Player defender) {
        forcePvP(attacker, defender, INFINITE_TIME);
    }

    public void stopPvP(Player player) {
        pvpManager.stopPvP(player);
    }

    public void stopPvPSilent(Player player) {
        pvpManager.stopPvPSilent(player);
    }

    public boolean isBypassed(Player player) {
        return pvpManager.isBypassed(player);
    }

    public boolean isInIgnoredWorld(Player player) {
        return pvpManager.isInIgnoredWorld(player);
    }

    public boolean isInIgnoredRegion(Player player) {
        return pvpManager.isInIgnoredRegion(player);
    }

    public boolean isPvPModeEnabled() {
        return pvpManager.isPvPModeEnabled();
    }

    public Set<Player> getPlayersInPvP() {
        return pvpManager.getPlayersInPvP();
    }

    public int getPlayersInPvPCount() {
        return pvpManager.getPlayersInPvPCount();
    }

    public PlayerData getPlayerData(Player player) {
        return pvpManager.getPlayerData(player);
    }

    public boolean hasCooldown(Player player, CooldownType type) {
        int cooldown = type.getCooldown(plugin.getSettings());
        return cooldown > 0 && cooldownManager.hasCooldown(player, type, cooldown * 1000L);
    }

    public long getCooldownRemaining(Player player, CooldownType type) {
        int cooldown = type.getCooldown(plugin.getSettings());
        if (cooldown <= 0 || !cooldownManager.hasCooldown(player, type, cooldown * 1000L)) {
            return 0;
        }
        return cooldownManager.getRemaining(player, type, cooldown * 1000L);
    }

    public int getCooldownRemainingSeconds(Player player, CooldownType type) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(getCooldownRemaining(player, type));
    }

    public boolean isItemBlockedInPvP(CooldownType type) {
        return type.getCooldown(plugin.getSettings()) < 0;
    }

    public int getItemCooldownSetting(CooldownType type) {
        return type.getCooldown(plugin.getSettings());
    }

    public void addInfiniteCooldown(Player player, CooldownType type) {
        cooldownManager.addInfiniteCooldown(player, type);
    }

    public void addCustomCooldown(Player player, CooldownType type, long durationMillis) {
        cooldownManager.addCooldown(player, type);
        cooldownManager.addItemCooldown(player, type, durationMillis);
    }

    public void removeCooldown(Player player, CooldownType type) {
        cooldownManager.removeCooldown(player, type);
    }

    public void removeAllCooldowns(Player player) {
        cooldownManager.remove(player);
    }

    public boolean hasInfiniteCooldown(Player player, CooldownType type) {
        return cooldownManager.hasInfiniteCooldown(player, type);
    }

    public void setBossbarVisible(Player player, boolean visible) {
        bossbarManager.setVisible(player, visible);
    }

    public boolean isBossbarVisible(Player player) {
        return bossbarManager.isVisible(player);
    }

    public void setBossbarEnabled(boolean enabled) {
        bossbarManager.setEnabled(enabled);
    }

    public boolean isBossbarEnabled() {
        return bossbarManager.isEnabled();
    }

    public PvPSnapshot createSnapshot(Player player) {
        return new PvPSnapshot(player, this);
    }

    public void startPvP(Player player) {
        pvpManager.forceStartPvP(player, plugin.getSettings().getPvpTime());
    }

    public void startPvP(Player player, int time) {
        pvpManager.forceStartPvP(player, time);
    }

    public void startInfinitePvP(Player player) {
        pvpManager.setPlayerPvPTime(player, INFINITE_TIME);
        pvpManager.forceStartPvP(player, INFINITE_TIME);
    }

    public void startPvPSilent(Player player) {
        pvpManager.forceStartPvPSilent(player, plugin.getSettings().getPvpTime());
    }

    public void startPvPSilent(Player player, int time) {
        pvpManager.forceStartPvPSilent(player, time);
    }

    public void startInfinitePvPSilent(Player player) {
        pvpManager.setPlayerPvPTime(player, INFINITE_TIME);
        pvpManager.forceStartPvPSilent(player, INFINITE_TIME);
    }

    public boolean startPvPChecked(Player player) {
        return pvpManager.forceStartPvPChecked(player, plugin.getSettings().getPvpTime());
    }

    public boolean startPvPChecked(Player player, int time) {
        return pvpManager.forceStartPvPChecked(player, time);
    }

    public boolean startPvPSilentChecked(Player player) {
        return pvpManager.forceStartPvPSilentChecked(player, plugin.getSettings().getPvpTime());
    }

    public boolean startPvPSilentChecked(Player player, int time) {
        return pvpManager.forceStartPvPSilentChecked(player, time);
    }

    public Antirelog getPlugin() {
        return plugin;
    }
}