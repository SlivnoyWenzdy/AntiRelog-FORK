package ru.leymooo.antirelog.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.leymooo.antirelog.config.CooldownNotificationEntry;
import ru.leymooo.antirelog.config.Settings;
import ru.leymooo.antirelog.event.PvpCooldownEvent;
import ru.leymooo.antirelog.event.PvpCooldownEvent.Action;
import ru.leymooo.antirelog.event.PvpStartedEvent;
import ru.leymooo.antirelog.event.PvpStoppedEvent;
import ru.leymooo.antirelog.manager.CooldownManager;
import ru.leymooo.antirelog.manager.CooldownManager.CooldownType;
import ru.leymooo.antirelog.manager.PvPManager;
import ru.leymooo.antirelog.util.ActionBar;
import ru.leymooo.antirelog.util.Utils;
import ru.leymooo.antirelog.util.VersionUtils;

import java.util.concurrent.TimeUnit;

public class CooldownListener implements Listener {

    private final CooldownManager cooldownManager;
    private final PvPManager pvpManager;
    private final Settings settings;

    public CooldownListener(Plugin plugin, CooldownManager cooldownManager, PvPManager pvpManager, Settings settings) {
        this.cooldownManager = cooldownManager;
        this.pvpManager = pvpManager;
        this.settings = settings;
        registerEntityResurrectEvent(plugin);
    }

    private void registerEntityResurrectEvent(Plugin plugin) {
        if (VersionUtils.isVersion(11)) {
            plugin.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
                public void onResurrect(EntityResurrectEvent event) {
                    if (event.getEntityType() != EntityType.PLAYER) {
                        return;
                    }
                    Player player = (Player) event.getEntity();
                    long cooldownMs = CooldownType.TOTEM.getCooldownMs(settings);
                    if (cooldownMs == 0 || pvpManager.isBypassed(player)) {
                        return;
                    }
                    if (cooldownMs < 0) {
                        cancelEventIfInPvp(event, CooldownType.TOTEM, player);
                        return;
                    }
                    if (checkCooldown(player, CooldownType.TOTEM, cooldownMs)) {
                        event.setCancelled(true);
                        return;
                    }
                    fireCooldownEvent(player, CooldownType.TOTEM, Action.ITEM_USED, cooldownMs);
                    cooldownManager.addCooldown(player, CooldownType.TOTEM, pvpManager.isInPvP(player));
                    addItemCooldownIfNeeded(player, CooldownType.TOTEM);
                }
            }, plugin);
        }
    }

    @EventHandler
    public void onItemEat(PlayerItemConsumeEvent event) {
        ItemStack consumeItem = event.getItem();

        CooldownType cooldownType = null;

        if (isChorus(consumeItem)) {
            cooldownType = CooldownType.CHORUS;
        }
        if (isGoldenOrEnchantedApple(consumeItem)) {
            cooldownType = isEnchantedGoldenApple(consumeItem) ? CooldownType.ENC_GOLDEN_APPLE : CooldownType.GOLDEN_APPLE;
        }

        CooldownType potionCooldownType = getPotionCooldownType(consumeItem);
        if (potionCooldownType != null) {
            cooldownType = potionCooldownType;
        }

        if (cooldownType == null) {
            return;
        }

        long cooldownMs = cooldownType.getCooldownMs(settings);
        if (cooldownMs == 0 || pvpManager.isBypassed(event.getPlayer())) {
            return;
        }
        if (cooldownMs < 0) {
            cancelPotionOrItemIfInPvp(event, cooldownType, event.getPlayer());
            return;
        }
        if (checkPotionOrItemCooldown(event.getPlayer(), cooldownType, cooldownMs)) {
            event.setCancelled(true);
            return;
        }
        fireCooldownEvent(event.getPlayer(), cooldownType, Action.ITEM_USED, cooldownMs);
        cooldownManager.addCooldown(event.getPlayer(), cooldownType, pvpManager.isInPvP(event.getPlayer()));
        addItemCooldownIfNeeded(event.getPlayer(), cooldownType);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof ThrownPotion) {
            handleThrownPotion(e, (ThrownPotion) e.getEntity());
            return;
        }

        if (e.getEntityType() == EntityType.THROWN_EXP_BOTTLE && e.getEntity().getShooter() instanceof Player) {
            handleExperienceBottle(e, (Player) e.getEntity().getShooter());
            return;
        }

        if (e.getEntityType() == EntityType.ENDER_PEARL && e.getEntity().getShooter() instanceof Player) {
            handleEnderPearl(e, (Player) e.getEntity().getShooter());
        }
    }

    private void handleThrownPotion(ProjectileLaunchEvent e, ThrownPotion thrownPotion) {
        if (!(thrownPotion.getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) thrownPotion.getShooter();
        if (pvpManager.isBypassed(player)) {
            return;
        }

        CooldownType cooldownType = getSplashPotionCooldownType(thrownPotion);
        if (cooldownType == null) {
            return;
        }

        long cooldownMs = cooldownType.getCooldownMs(settings);
        if (cooldownMs == 0) {
            return;
        }
        if (cooldownMs < 0) {
            if (pvpManager.isInPvP(player)) {
                e.setCancelled(true);
                CooldownNotificationEntry notification = cooldownType.getNotification(settings);
                if (notification.isBlockedMessage()) {
                    String message = settings.getMessages().getPotionDisabledInPvp();
                    if (!message.isEmpty()) {
                        player.sendMessage(Utils.color(message));
                    }
                }
                fireCooldownEvent(player, cooldownType, Action.ITEM_BLOCKED, 0);
            }
            return;
        }
        if (checkPotionCooldown(player, cooldownType, cooldownMs)) {
            e.setCancelled(true);
            return;
        }
        fireCooldownEvent(player, cooldownType, Action.ITEM_USED, cooldownMs);
        cooldownManager.addCooldown(player, cooldownType, pvpManager.isInPvP(player));
        addItemCooldownIfNeeded(player, cooldownType);
    }

    private void handleExperienceBottle(ProjectileLaunchEvent e, Player player) {
        if (pvpManager.isBypassed(player)) {
            return;
        }
        long cooldownMs = CooldownType.EXPERIENCE_BOTTLE.getCooldownMs(settings);
        if (cooldownMs == 0) {
            return;
        }
        if (cooldownMs < 0) {
            if (pvpManager.isInPvP(player)) {
                e.setCancelled(true);
                CooldownNotificationEntry notification = CooldownType.EXPERIENCE_BOTTLE.getNotification(settings);
                if (notification.isBlockedMessage()) {
                    String message = settings.getMessages().getItemDisabledInPvp();
                    if (!message.isEmpty()) {
                        player.sendMessage(Utils.color(message));
                    }
                }
                fireCooldownEvent(player, CooldownType.EXPERIENCE_BOTTLE, Action.ITEM_BLOCKED, 0);
            }
            return;
        }
        if (checkCooldown(player, CooldownType.EXPERIENCE_BOTTLE, cooldownMs)) {
            e.setCancelled(true);
            return;
        }
        fireCooldownEvent(player, CooldownType.EXPERIENCE_BOTTLE, Action.ITEM_USED, cooldownMs);
        cooldownManager.addCooldown(player, CooldownType.EXPERIENCE_BOTTLE, pvpManager.isInPvP(player));
        addItemCooldownIfNeeded(player, CooldownType.EXPERIENCE_BOTTLE);
    }

    private void handleEnderPearl(ProjectileLaunchEvent e, Player player) {
        long cooldownMs = CooldownType.ENDER_PEARL.getCooldownMs(settings);
        if (cooldownMs <= 0) {
            return;
        }
        if (!pvpManager.isBypassed(player)) {
            fireCooldownEvent(player, CooldownType.ENDER_PEARL, Action.ITEM_USED, cooldownMs);
            cooldownManager.addCooldown(player, CooldownType.ENDER_PEARL, pvpManager.isInPvP(player));
            addItemCooldownIfNeeded(player, CooldownType.ENDER_PEARL);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!pvpManager.isInPvP(player)) {
            return;
        }

        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null) {
            return;
        }

        CooldownType cooldownType = null;

        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
            cooldownType = getPotionCooldownType(item);
        } else if (item.getType() == Material.GOLDEN_APPLE) {
            cooldownType = isEnchantedGoldenApple(item) ? CooldownType.ENC_GOLDEN_APPLE : CooldownType.GOLDEN_APPLE;
        } else if (VersionUtils.isVersion(13) && item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            cooldownType = CooldownType.ENC_GOLDEN_APPLE;
        } else if (item.getType() == Material.ENDER_PEARL) {
            cooldownType = CooldownType.ENDER_PEARL;
        } else if (isChorus(item)) {
            cooldownType = CooldownType.CHORUS;
        } else if (isFirework(item)) {
            cooldownType = CooldownType.FIREWORK;
        } else if (item.getType() == Material.EXPERIENCE_BOTTLE) {
            cooldownType = CooldownType.EXPERIENCE_BOTTLE;
        }

        if (cooldownType == null) {
            return;
        }

        long cooldownMs = cooldownType.getCooldownMs(settings);
        if (cooldownMs == 0) {
            return;
        }

        CooldownNotificationEntry notification = cooldownType.getNotification(settings);
        boolean isPotion = isPotionCooldownType(cooldownType);

        if (cooldownMs < 0) {
            if (notification.isActionbarMessage()) {
                String message = isPotion ? settings.getMessages().getPotionDisabledInPvpActionbar() :
                        settings.getMessages().getItemDisabledInPvpActionbar();
                if (!message.isEmpty()) {
                    ActionBar.sendAction(player, Utils.color(message));
                }
            }
            return;
        }

        if (cooldownManager.hasCooldown(player, cooldownType, cooldownMs) && cooldownManager.wasCooldownSetInPvP(player, cooldownType)) {
            if (notification.isActionbarMessage()) {
                long remaining = cooldownManager.getRemaining(player, cooldownType, cooldownMs);
                int remainingInt = (int) TimeUnit.MILLISECONDS.toSeconds(remaining);
                String message = isPotion ? settings.getMessages().getPotionCooldownActionbar() :
                        settings.getMessages().getItemCooldownActionbar();
                if (!message.isEmpty()) {
                    ActionBar.sendAction(player, Utils.color(Utils.replaceTime(message, remainingInt)));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        if (pvpManager.isBypassed(event.getPlayer())) return;

        Material itemType = event.getItem().getType();

        if (itemType == Material.ENDER_PEARL) {
            long cooldownMs = CooldownType.ENDER_PEARL.getCooldownMs(settings);
            if (cooldownMs == 0) return;
            if (cooldownMs < 0) {
                cancelEventIfInPvp(event, CooldownType.ENDER_PEARL, event.getPlayer());
                return;
            }
            if (checkCooldown(event.getPlayer(), CooldownType.ENDER_PEARL, cooldownMs)) {
                event.setCancelled(true);
            }
        } else if (isFirework(event.getItem())) {
            long cooldownMs = CooldownType.FIREWORK.getCooldownMs(settings);
            if (cooldownMs == 0) return;
            if (cooldownMs < 0) {
                cancelEventIfInPvp(event, CooldownType.FIREWORK, event.getPlayer());
                return;
            }
            if (checkCooldown(event.getPlayer(), CooldownType.FIREWORK, cooldownMs)) {
                event.setCancelled(true);
                return;
            }
            fireCooldownEvent(event.getPlayer(), CooldownType.FIREWORK, Action.ITEM_USED, cooldownMs);
            cooldownManager.addCooldown(event.getPlayer(), CooldownType.FIREWORK, pvpManager.isInPvP(event.getPlayer()));
            addItemCooldownIfNeeded(event.getPlayer(), CooldownType.FIREWORK);
        } else if (itemType == Material.EXPERIENCE_BOTTLE) {
            long cooldownMs = CooldownType.EXPERIENCE_BOTTLE.getCooldownMs(settings);
            if (cooldownMs == 0) return;
            if (cooldownMs < 0) {
                cancelEventIfInPvp(event, CooldownType.EXPERIENCE_BOTTLE, event.getPlayer());
                return;
            }
            if (checkCooldown(event.getPlayer(), CooldownType.EXPERIENCE_BOTTLE, cooldownMs)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldownManager.remove(event.getPlayer());
    }

    @EventHandler
    public void onPvpStart(PvpStartedEvent event) {
        Player attacker = event.getAttacker();
        Player defender = event.getDefender();

        switch (event.getPvpStatus()) {
            case ALL_NOT_IN_PVP:
                cooldownManager.enteredToPvp(defender);
                if (attacker != defender) {
                    cooldownManager.enteredToPvp(attacker);
                }
                break;
            case ATTACKER_IN_PVP:
                cooldownManager.enteredToPvp(defender);
                break;
            case DEFENDER_IN_PVP:
                cooldownManager.enteredToPvp(attacker);
                break;
        }
    }

    @EventHandler
    public void onPvpStop(PvpStoppedEvent event) {
        cooldownManager.removedFromPvp(event.getPlayer());
    }

    private CooldownType getPotionCooldownType(ItemStack item) {
        if (item == null) {
            return null;
        }
        Material type = item.getType();
        if (type != Material.POTION && type != Material.SPLASH_POTION && type != Material.LINGERING_POTION) {
            return null;
        }
        if (!(item.getItemMeta() instanceof PotionMeta)) {
            return null;
        }
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        return matchPotionType(meta);
    }

    private CooldownType getSplashPotionCooldownType(ThrownPotion thrownPotion) {
        ItemStack item = thrownPotion.getItem();
        if (!(item.getItemMeta() instanceof PotionMeta)) {
            return null;
        }
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        return matchPotionType(meta);
    }

    private CooldownType matchPotionType(PotionMeta meta) {
        try {
            PotionData data = meta.getBasePotionData();
            if (data != null) {
                String typeName = data.getType().name();
                if (typeName.equals("INSTANT_HEAL") || typeName.equals("HEALING")) {
                    return CooldownType.HEALING_POTION;
                }
                if (typeName.equals("REGEN") || typeName.equals("REGENERATION")) {
                    return CooldownType.REGENERATION_POTION;
                }
                if (typeName.equals("STRENGTH") || typeName.equals("INCREASE_DAMAGE")) {
                    return CooldownType.STRENGTH_POTION;
                }
                if (typeName.equals("SPEED") || typeName.equals("SWIFTNESS")) {
                    return CooldownType.SPEED_POTION;
                }
            }
        } catch (Exception ignored) {
        }

        if (meta.hasCustomEffects()) {
            for (PotionEffect effect : meta.getCustomEffects()) {
                CooldownType type = matchEffectType(effect.getType());
                if (type != null) {
                    return type;
                }
            }
        }

        return null;
    }

    private CooldownType matchEffectType(PotionEffectType effectType) {
        String name = effectType.getName();
        if (name.equals("HEAL") || name.equals("INSTANT_HEALTH")) {
            return CooldownType.HEALING_POTION;
        }
        if (name.equals("REGENERATION")) {
            return CooldownType.REGENERATION_POTION;
        }
        if (name.equals("INCREASE_DAMAGE") || name.equals("STRENGTH")) {
            return CooldownType.STRENGTH_POTION;
        }
        if (name.equals("SPEED")) {
            return CooldownType.SPEED_POTION;
        }
        return null;
    }

    private boolean isPotionCooldownType(CooldownType type) {
        return type == CooldownType.HEALING_POTION
                || type == CooldownType.REGENERATION_POTION
                || type == CooldownType.STRENGTH_POTION
                || type == CooldownType.SPEED_POTION;
    }

    private boolean isChorus(ItemStack itemStack) {
        return VersionUtils.isVersion(9) && itemStack.getType() == Material.CHORUS_FRUIT;
    }

    private boolean isGoldenOrEnchantedApple(ItemStack itemStack) {
        return isGoldenApple(itemStack) || isEnchantedGoldenApple(itemStack);
    }

    private boolean isGoldenApple(ItemStack itemStack) {
        return itemStack.getType() == Material.GOLDEN_APPLE;
    }

    private boolean isEnchantedGoldenApple(ItemStack itemStack) {
        return (VersionUtils.isVersion(13) && itemStack.getType() == Material.ENCHANTED_GOLDEN_APPLE)
                || (isGoldenApple(itemStack) && itemStack.getDurability() >= 1);
    }

    private boolean isFirework(ItemStack itemStack) {
        return VersionUtils.isVersion(13) ? itemStack.getType() == Material.FIREWORK_ROCKET : itemStack.getType() == Material.getMaterial("FIREWORK");
    }

    private void cancelEventIfInPvp(Cancellable event, CooldownType type, Player player) {
        if (pvpManager.isInPvP(player)) {
            event.setCancelled(true);
            fireCooldownEvent(player, type, Action.ITEM_BLOCKED, 0);
            CooldownNotificationEntry notification = type.getNotification(settings);
            if (notification.isBlockedMessage()) {
                String message = type == CooldownType.TOTEM ? settings.getMessages().getTotemDisabledInPvp() :
                        settings.getMessages().getItemDisabledInPvp();
                if (!message.isEmpty()) {
                    player.sendMessage(Utils.color(message));
                }
            }
        }
    }

    private void cancelPotionOrItemIfInPvp(Cancellable event, CooldownType type, Player player) {
        if (pvpManager.isInPvP(player)) {
            event.setCancelled(true);
            fireCooldownEvent(player, type, Action.ITEM_BLOCKED, 0);
            CooldownNotificationEntry notification = type.getNotification(settings);
            if (notification.isBlockedMessage()) {
                String message = isPotionCooldownType(type) ? settings.getMessages().getPotionDisabledInPvp() :
                        (type == CooldownType.TOTEM ? settings.getMessages().getTotemDisabledInPvp() :
                                settings.getMessages().getItemDisabledInPvp());
                if (!message.isEmpty()) {
                    player.sendMessage(Utils.color(message));
                }
            }
        }
    }

    private boolean checkCooldown(Player player, CooldownType cooldownType, long cooldownMs) {
        boolean cooldownActive = !pvpManager.isPvPModeEnabled() || pvpManager.isInPvP(player);
        if (cooldownActive && cooldownManager.hasCooldown(player, cooldownType, cooldownMs) && cooldownManager.wasCooldownSetInPvP(player, cooldownType)) {
            long remaining = cooldownManager.getRemaining(player, cooldownType, cooldownMs);
            int remainingInt = (int) TimeUnit.MILLISECONDS.toSeconds(remaining);
            fireCooldownEvent(player, cooldownType, Action.COOLDOWN_ACTIVE, remaining);
            CooldownNotificationEntry notification = cooldownType.getNotification(settings);
            if (notification.isChatMessage()) {
                String message = cooldownType == CooldownType.TOTEM ? settings.getMessages().getTotemCooldown() :
                        settings.getMessages().getItemCooldown();
                if (!message.isEmpty()) {
                    player.sendMessage(Utils.color(Utils.replaceTime(message, remainingInt)));
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkPotionCooldown(Player player, CooldownType cooldownType, long cooldownMs) {
        boolean cooldownActive = !pvpManager.isPvPModeEnabled() || pvpManager.isInPvP(player);
        if (cooldownActive && cooldownManager.hasCooldown(player, cooldownType, cooldownMs) && cooldownManager.wasCooldownSetInPvP(player, cooldownType)) {
            long remaining = cooldownManager.getRemaining(player, cooldownType, cooldownMs);
            int remainingInt = (int) TimeUnit.MILLISECONDS.toSeconds(remaining);
            fireCooldownEvent(player, cooldownType, Action.COOLDOWN_ACTIVE, remaining);
            CooldownNotificationEntry notification = cooldownType.getNotification(settings);
            if (notification.isChatMessage()) {
                String message = settings.getMessages().getPotionCooldown();
                if (!message.isEmpty()) {
                    player.sendMessage(Utils.color(Utils.replaceTime(message, remainingInt)));
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkPotionOrItemCooldown(Player player, CooldownType cooldownType, long cooldownMs) {
        if (isPotionCooldownType(cooldownType)) {
            return checkPotionCooldown(player, cooldownType, cooldownMs);
        }
        return checkCooldown(player, cooldownType, cooldownMs);
    }

    private void addItemCooldownIfNeeded(Player player, CooldownType cooldownType) {
        long cooldownMs = cooldownType.getCooldownMs(settings);
        if (pvpManager.isPvPModeEnabled()) {
            if (pvpManager.isInPvP(player)) {
                cooldownManager.addItemCooldown(player, cooldownType, cooldownMs);
            }
        } else {
            cooldownManager.addItemCooldown(player, cooldownType, cooldownMs);
        }
    }

    private void fireCooldownEvent(Player player, CooldownType type, Action action, long duration) {
        Bukkit.getPluginManager().callEvent(new PvpCooldownEvent(player, type, action, duration));
    }
}