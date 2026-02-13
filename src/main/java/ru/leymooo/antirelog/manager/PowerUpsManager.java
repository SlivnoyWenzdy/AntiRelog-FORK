package ru.leymooo.antirelog.manager;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import de.myzelyam.api.vanish.VanishAPI;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.kitteh.vanish.VanishPlugin;
import ru.leymooo.antirelog.config.Settings;
import ru.leymooo.antirelog.event.PowerUpsDisabledEvent;
import ru.leymooo.antirelog.event.PowerUpsDisabledEvent.PowerUpType;
import ru.leymooo.antirelog.util.Utils;

import java.util.EnumSet;
import java.util.Set;

public class PowerUpsManager {

    private final Settings settings;

    private boolean vanishAPI, libsDisguises, cmi;
    private VanishPlugin vanishNoPacket;
    private Essentials essentials;

    public PowerUpsManager(Settings settings) {
        this.settings = settings;
        detectPlugins();
    }

    public Set<PowerUpType> disablePowerUps(Player player, Player triggeredBy) {
        Set<PowerUpType> disabled = EnumSet.noneOf(PowerUpType.class);

        if (player.hasPermission("antirelog.bypass.checks")) {
            return disabled;
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            if (Bukkit.getDefaultGameMode() == GameMode.ADVENTURE) {
                player.setGameMode(GameMode.ADVENTURE);
            } else {
                player.setGameMode(GameMode.SURVIVAL);
            }
            disabled.add(PowerUpType.CREATIVE);
        }

        if (player.isFlying() || player.getAllowFlight()) {
            player.setFlying(false);
            player.setAllowFlight(false);
            disabled.add(PowerUpType.FLY);
        }

        disabled.addAll(checkEssentials(player));
        disabled.addAll(checkCMI(player));

        if (vanishAPI && VanishAPI.isInvisible(player)) {
            VanishAPI.showPlayer(player);
            disabled.add(PowerUpType.SUPERVANISH);
        }
        if (vanishNoPacket != null && vanishNoPacket.getManager().isVanished(player)) {
            vanishNoPacket.getManager().toggleVanishQuiet(player, false);
            disabled.add(PowerUpType.VANISH_NO_PACKET);
        }
        if (libsDisguises && DisguiseAPI.isSelfDisguised(player)) {
            DisguiseAPI.undisguiseToAll(player);
            disabled.add(PowerUpType.LIBS_DISGUISES);
        }

        if (!disabled.isEmpty()) {
            Bukkit.getPluginManager().callEvent(new PowerUpsDisabledEvent(player, triggeredBy, disabled));
        }

        return disabled;
    }

    public void disablePowerUpsWithRunCommands(Player player, Player triggeredBy) {
        Set<PowerUpType> disabled = disablePowerUps(player, triggeredBy);
        if (!disabled.isEmpty() && !settings.getCommandsOnPowerupsDisable().isEmpty()) {
            settings.getCommandsOnPowerupsDisable().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    Utils.color(command.replace("%player%", player.getName()))));
            String message = settings.getMessages().getPvpStartedWithPowerups();
            if (!message.isEmpty()) {
                player.sendMessage(Utils.color(message));
            }
        }
    }

    public void detectPlugins() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        this.vanishAPI = pluginManager.isPluginEnabled("SuperVanish") || pluginManager.isPluginEnabled("PremiumVanish");
        this.vanishNoPacket = pluginManager.isPluginEnabled("VanishNoPacket") ? (VanishPlugin) pluginManager.getPlugin("VanishNoPacket")
                : null;
        this.essentials = pluginManager.isPluginEnabled("Essentials") ? (Essentials) pluginManager.getPlugin("Essentials") : null;
        this.libsDisguises = pluginManager.isPluginEnabled("LibsDisguises");
        this.cmi = pluginManager.isPluginEnabled("CMI");
    }

    private Set<PowerUpType> checkEssentials(Player player) {
        Set<PowerUpType> disabled = EnumSet.noneOf(PowerUpType.class);
        if (essentials != null) {
            User user = essentials.getUser(player);
            if (user.isVanished()) {
                user.setVanished(false);
                disabled.add(PowerUpType.ESSENTIALS_VANISH);
            }
            if (user.isGodModeEnabled()) {
                user.setGodModeEnabled(false);
                disabled.add(PowerUpType.ESSENTIALS_GOD);
            }
        }
        return disabled;
    }

    private Set<PowerUpType> checkCMI(Player player) {
        Set<PowerUpType> disabled = EnumSet.noneOf(PowerUpType.class);
        if (cmi) {
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            if (user != null) {
                if (user.isGod()) {
                    CMI.getInstance().getNMS().changeGodMode(player, false);
                    user.setTgod(0);
                    disabled.add(PowerUpType.CMI_GOD);
                }
                if (user.isVanished()) {
                    user.setVanished(false);
                    disabled.add(PowerUpType.CMI_VANISH);
                }
            }
        }
        return disabled;
    }
}