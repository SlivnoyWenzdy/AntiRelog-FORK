package ru.leymooo.antirelog.manager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import ru.leymooo.antirelog.Antirelog;
import ru.leymooo.antirelog.api.AntiRelogAPI;
import ru.leymooo.antirelog.config.Settings;
import ru.leymooo.antirelog.data.PlayerData;
import ru.leymooo.antirelog.event.*;
import ru.leymooo.antirelog.event.PvpPreStartEvent.PvPStatus;
import ru.leymooo.antirelog.event.PvpStoppedEvent.StopReason;
import ru.leymooo.antirelog.event.PvpTimeUpdateEvent.UpdateReason;
import ru.leymooo.antirelog.util.ActionBar;
import ru.leymooo.antirelog.util.CommandMapUtils;
import ru.leymooo.antirelog.util.Utils;
import ru.leymooo.antirelog.util.VersionUtils;

import java.util.*;

public class PvPManager {

    private final Settings settings;
    private final Antirelog plugin;
    private final Map<Player, Integer> pvpMap = new HashMap<>();
    private final Map<Player, Integer> silentPvpMap = new HashMap<>();
    private final Map<Player, Integer> customPvpTimes = new HashMap<>();
    private final PowerUpsManager powerUpsManager;
    private final BossbarManager bossbarManager;
    private final Set<String> whiteListedCommands = new HashSet<>();

    public PvPManager(Settings settings, Antirelog plugin) {
        this.settings = settings;
        this.plugin = plugin;
        this.powerUpsManager = new PowerUpsManager(settings);
        this.bossbarManager = new BossbarManager(settings);
        onPluginEnable();
    }

    public void onPluginDisable() {
        Set<Player> players = new HashSet<>(pvpMap.keySet());
        players.forEach(p -> stopPvPWithReason(p, StopReason.PLUGIN_RELOAD));
        Set<Player> silentPlayers = new HashSet<>(silentPvpMap.keySet());
        silentPlayers.forEach(this::stopPvPSilentInternal);
        customPvpTimes.clear();
        this.bossbarManager.clearBossbars();
    }

    public void onPluginEnable() {
        whiteListedCommands.clear();
        if (settings.isDisableCommandsInPvp() && !settings.getWhiteListedCommands().isEmpty()) {
            settings.getWhiteListedCommands().forEach(wcommand -> {
                Command command = CommandMapUtils.getCommand(wcommand);
                whiteListedCommands.add(wcommand.toLowerCase());
                if (command != null) {
                    whiteListedCommands.add(command.getName().toLowerCase());
                    command.getAliases().forEach(alias -> whiteListedCommands.add(alias.toLowerCase()));
                }
            });
        }
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (pvpMap.isEmpty() && silentPvpMap.isEmpty()) {
                return;
            }
            iterateMap(pvpMap, false);
            iterateMap(silentPvpMap, true);
        }, 20, 20);
        this.bossbarManager.createBossBars();
    }

    private void iterateMap(Map<Player, Integer> map, boolean bypassed) {
        if (map.isEmpty()) {
            return;
        }
        List<Player> playersInPvp = new ArrayList<>(map.keySet());
        for (Player player : playersInPvp) {
            int currentTime = bypassed ? getTimeRemainingInPvPSilent(player) : getTimeRemainingInPvP(player);

            if (settings.isDisablePvpInIgnoredRegion() && isInIgnoredRegion(player)) {
                if (bypassed) {
                    stopPvPSilentInternal(player);
                    Bukkit.getPluginManager().callEvent(new PvpStoppedEvent(player, StopReason.ENTERED_IGNORED_REGION, currentTime));
                } else {
                    stopPvPWithReason(player, StopReason.ENTERED_IGNORED_REGION);
                }
                continue;
            }

            if (isInfinitePvP(player)) {
                updatePvpMode(player, bypassed, currentTime);
                continue;
            }

            int timeRemaining = currentTime - 1;
            if (timeRemaining <= 0) {
                if (bypassed) {
                    stopPvPSilentInternal(player);
                    Bukkit.getPluginManager().callEvent(new PvpStoppedEvent(player, StopReason.EXPIRED, currentTime));
                } else {
                    stopPvPWithReason(player, StopReason.EXPIRED);
                }
            } else {
                PvpTimeUpdateEvent updateEvent = new PvpTimeUpdateEvent(player, currentTime, timeRemaining, UpdateReason.TICK);
                Bukkit.getPluginManager().callEvent(updateEvent);
                if (!updateEvent.isCancelled()) {
                    updatePvpMode(player, bypassed, updateEvent.getNewTime());
                }
            }
        }
    }

    public boolean isInPvP(Player player) {
        return pvpMap.containsKey(player);
    }

    public boolean isInSilentPvP(Player player) {
        return silentPvpMap.containsKey(player);
    }

    public int getTimeRemainingInPvP(Player player) {
        return pvpMap.getOrDefault(player, 0);
    }

    public int getTimeRemainingInPvPSilent(Player player) {
        return silentPvpMap.getOrDefault(player, 0);
    }

    public Set<Player> getPlayersInPvP() {
        return Collections.unmodifiableSet(new HashSet<>(pvpMap.keySet()));
    }

    public int getPlayersInPvPCount() {
        return pvpMap.size();
    }

    public int getPlayerPvPTime(Player player) {
        return customPvpTimes.getOrDefault(player, settings.getPvpTime());
    }

    public void setPlayerPvPTime(Player player, int time) {
        customPvpTimes.put(player, time);
    }

    public void removePlayerPvPTime(Player player) {
        customPvpTimes.remove(player);
    }

    public boolean hasCustomPvPTime(Player player) {
        return customPvpTimes.containsKey(player);
    }

    public boolean isInfinitePvP(Player player) {
        Integer custom = customPvpTimes.get(player);
        return custom != null && custom == AntiRelogAPI.INFINITE_TIME;
    }

    public PlayerData getPlayerData(Player player) {
        return bossbarManager.getPlayerData(player);
    }

    public void playerDamagedByPlayer(Player attacker, Player defender) {
        if (defender != attacker && attacker != null && defender != null && (attacker.getWorld() == defender.getWorld())) {
            if (defender.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (attacker.hasMetadata("NPC") || defender.hasMetadata("NPC")) {
                return;
            }
            if (defender.isDead() || attacker.isDead()) {
                return;
            }
            tryStartPvP(attacker, defender);
        }
    }

    private void tryStartPvP(Player attacker, Player defender) {
        if (isInIgnoredWorld(attacker)) {
            return;
        }

        if (isInIgnoredRegion(attacker) || isInIgnoredRegion(defender)) {
            return;
        }

        if (!isPvPModeEnabled() && settings.isDisablePowerups()) {
            if (!isHasBypassPermission(attacker)) {
                powerUpsManager.disablePowerUpsWithRunCommands(attacker, defender);
            }
            if (!isHasBypassPermission(defender)) {
                powerUpsManager.disablePowerUps(defender, attacker);
            }
            return;
        }

        if (!isPvPModeEnabled()) {
            return;
        }

        boolean attackerBypassed = isHasBypassPermission(attacker);
        boolean defenderBypassed = isHasBypassPermission(defender);

        if (attackerBypassed && defenderBypassed) {
            return;
        }

        boolean attackerInPvp = isInPvP(attacker) || isInSilentPvP(attacker);
        boolean defenderInPvp = isInPvP(defender) || isInSilentPvP(defender);

        if (attackerInPvp && defenderInPvp) {
            updateAttackerAndCallEvent(attacker, defender, attackerBypassed);
            updateDefenderAndCallEvent(defender, attacker, defenderBypassed);
            return;
        }

        PvPStatus pvpStatus = PvPStatus.ALL_NOT_IN_PVP;
        if (attackerInPvp) {
            pvpStatus = PvPStatus.ATTACKER_IN_PVP;
        } else if (defenderInPvp) {
            pvpStatus = PvPStatus.DEFENDER_IN_PVP;
        }

        PvpPreStartEvent preStartEvent = new PvpPreStartEvent(defender, attacker, settings.getPvpTime(), pvpStatus);
        Bukkit.getPluginManager().callEvent(preStartEvent);
        if (preStartEvent.isCancelled()) {
            return;
        }

        int pvpTime = preStartEvent.getPvpTime();

        if (pvpStatus == PvPStatus.ATTACKER_IN_PVP) {
            updateAttackerAndCallEvent(attacker, defender, attackerBypassed);
            startPvp(defender, defenderBypassed, false, pvpTime);
        } else if (pvpStatus == PvPStatus.DEFENDER_IN_PVP) {
            updateDefenderAndCallEvent(defender, attacker, defenderBypassed);
            startPvp(attacker, attackerBypassed, true, pvpTime);
        } else {
            startPvp(attacker, attackerBypassed, true, pvpTime);
            startPvp(defender, defenderBypassed, false, pvpTime);
        }

        Bukkit.getPluginManager().callEvent(new PvpStartedEvent(defender, attacker, pvpTime, pvpStatus));
    }

    private void startPvp(Player player, boolean bypassed, boolean attacker, int pvpTime) {
        if (!hasCustomPvPTime(player)) {
            customPvpTimes.put(player, pvpTime);
        }
        int actualTime = getPlayerPvPTime(player);
        bossbarManager.createPlayerData(player, actualTime, bypassed);
        if (!bypassed) {
            String message = Utils.color(settings.getMessages().getPvpStarted());
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
            if (attacker && settings.isDisablePowerups()) {
                powerUpsManager.disablePowerUpsWithRunCommands(player, null);
            }
            sendTitles(player, true);
        }
        updatePvpMode(player, bypassed, actualTime > 0 ? actualTime : pvpTime);
        player.setNoDamageTicks(0);
    }

    private void updatePvpMode(Player player, boolean bypassed, int newTime) {
        if (bypassed) {
            silentPvpMap.put(player, newTime);
        } else {
            pvpMap.put(player, newTime);
            PlayerData data = bossbarManager.getPlayerData(player);
            if (data != null) {
                data.setDelay(newTime);
                data.updateBossBar(settings);
            }
            String actionBar = settings.getMessages().getInPvpActionbar();
            if (!actionBar.isEmpty()) {
                if (isInfinitePvP(player)) {
                    sendActionBar(player, Utils.color(actionBar.replace("%time%", "∞").replace("%formated-sec%", "")));
                } else {
                    sendActionBar(player, Utils.color(Utils.replaceTime(actionBar, newTime)));
                }
            }
            if (settings.isDisablePowerups()) {
                powerUpsManager.disablePowerUps(player, null);
            }
        }
    }

    private void updateAttackerAndCallEvent(Player attacker, Player defender, boolean bypassed) {
        int oldTime = bypassed ? getTimeRemainingInPvPSilent(attacker) : getTimeRemainingInPvP(attacker);
        if (isInfinitePvP(attacker)) {
            return;
        }
        int resetTime = getPlayerPvPTime(attacker);
        PvpTimeUpdateEvent event = new PvpTimeUpdateEvent(attacker, oldTime, resetTime, UpdateReason.DAMAGE_DEALT);
        event.setDamagedPlayer(defender);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            updatePvpMode(attacker, bypassed, event.getNewTime());
        }
    }

    private void updateDefenderAndCallEvent(Player defender, Player attackedBy, boolean bypassed) {
        int oldTime = bypassed ? getTimeRemainingInPvPSilent(defender) : getTimeRemainingInPvP(defender);
        if (isInfinitePvP(defender)) {
            return;
        }
        int resetTime = getPlayerPvPTime(defender);
        PvpTimeUpdateEvent event = new PvpTimeUpdateEvent(defender, oldTime, resetTime, UpdateReason.DAMAGE_RECEIVED);
        event.setDamagedBy(attackedBy);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            updatePvpMode(defender, bypassed, event.getNewTime());
        }
    }

    public void stopPvP(Player player) {
        stopPvPWithReason(player, StopReason.API_CALL);
    }

    public void stopPvPWithReason(Player player, StopReason reason) {
        int timeWhenStopped = getTimeRemainingInPvP(player);
        if (timeWhenStopped <= 0) {
            timeWhenStopped = getTimeRemainingInPvPSilent(player);
        }
        stopPvPSilentInternal(player);
        sendTitles(player, false);
        String message = Utils.color(settings.getMessages().getPvpStopped());
        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
        String actionBar = settings.getMessages().getPvpStoppedActionbar();
        if (!actionBar.isEmpty()) {
            sendActionBar(player, Utils.color(actionBar));
        }
        Bukkit.getPluginManager().callEvent(new PvpStoppedEvent(player, reason, timeWhenStopped));
    }

    public void stopPvPSilent(Player player) {
        stopPvPSilentWithEvent(player, StopReason.API_CALL);
    }

    public void stopPvPSilentWithEvent(Player player, StopReason reason) {
        int timeWhenStopped = getTimeRemainingInPvP(player);
        if (timeWhenStopped <= 0) {
            timeWhenStopped = getTimeRemainingInPvPSilent(player);
        }
        stopPvPSilentInternal(player);
        Bukkit.getPluginManager().callEvent(new PvpStoppedEvent(player, reason, timeWhenStopped));
    }

    private void stopPvPSilentInternal(Player player) {
        pvpMap.remove(player);
        silentPvpMap.remove(player);
        customPvpTimes.remove(player);
        bossbarManager.clearBossbar(player);
    }

    public boolean isCommandWhiteListed(String command) {
        if (whiteListedCommands.isEmpty()) {
            return false;
        }
        return whiteListedCommands.contains(command.toLowerCase());
    }

    public PowerUpsManager getPowerUpsManager() {
        return powerUpsManager;
    }

    public BossbarManager getBossbarManager() {
        return bossbarManager;
    }

    private void sendTitles(Player player, boolean isPvpStarted) {
        String title = isPvpStarted ? settings.getMessages().getPvpStartedTitle() : settings.getMessages().getPvpStoppedTitle();
        String subtitle = isPvpStarted ? settings.getMessages().getPvpStartedSubtitle() : settings.getMessages().getPvpStoppedSubtitle();
        title = title.isEmpty() ? null : Utils.color(title);
        subtitle = subtitle.isEmpty() ? null : Utils.color(subtitle);
        if (title == null && subtitle == null) {
            return;
        }
        if (VersionUtils.isVersion(11)) {
            player.sendTitle(title, subtitle, 10, 30, 10);
        } else {
            player.sendTitle(title, subtitle);
        }
    }

    private void sendActionBar(Player player, String message) {
        ActionBar.sendAction(player, message);
    }

    public boolean isPvPModeEnabled() {
        return settings.getPvpTime() > 0;
    }

    public boolean isBypassed(Player player) {
        return isHasBypassPermission(player) || isInIgnoredWorld(player);
    }

    public boolean isHasBypassPermission(Player player) {
        return player.hasPermission("antirelog.bypass");
    }

    public boolean isInIgnoredWorld(Player player) {
        return settings.getDisabledWorlds().contains(player.getWorld().getName().toLowerCase());
    }

    public boolean isInIgnoredRegion(Player player) {
        if (!plugin.isWorldguardEnabled() || settings.getIgnoredWgRegions().isEmpty()) {
            return false;
        }
        Set<String> regions = settings.getIgnoredWgRegions();
        Set<IWrappedRegion> wrappedRegions = WorldGuardWrapper.getInstance().getRegions(player.getLocation());
        if (wrappedRegions.isEmpty()) {
            return false;
        }
        for (IWrappedRegion region : wrappedRegions) {
            if (regions.contains(region.getId().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}