package ru.leymooo.antirelog.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.leymooo.antirelog.Antirelog;
import ru.leymooo.antirelog.config.BlockedBlocksNotifications;
import ru.leymooo.antirelog.config.Messages;
import ru.leymooo.antirelog.config.Settings;
import ru.leymooo.antirelog.event.PvpPlayerKilledEvent;
import ru.leymooo.antirelog.event.PvpPlayerKilledEvent.KillReason;
import ru.leymooo.antirelog.event.PvpStoppedEvent.StopReason;
import ru.leymooo.antirelog.manager.PvPManager;
import ru.leymooo.antirelog.util.ActionBar;
import ru.leymooo.antirelog.util.Utils;
import ru.leymooo.antirelog.util.VersionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PvPListener implements Listener {

    private static final String META_KEY = "ar-f-shooter";

    private final Antirelog plugin;
    private final PvPManager pvpManager;
    private final Messages messages;
    private final Settings settings;
    private final Map<Player, AtomicInteger> allowedTeleports = new HashMap<>();

    public PvPListener(Antirelog plugin, PvPManager pvpManager, Settings settings) {
        this.plugin = plugin;
        this.pvpManager = pvpManager;
        this.settings = settings;
        this.messages = settings.getMessages();
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            allowedTeleports.values().forEach(ai -> ai.set(ai.get() + 1));
            allowedTeleports.values().removeIf(ai -> ai.get() >= 5);
        }, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        Player target = (Player) event.getEntity();
        Player damager = getDamager(event.getDamager());
        pvpManager.playerDamagedByPlayer(damager, target);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractWithEntity(PlayerInteractEntityEvent event) {
        if (settings.isCancelInteractWithEntities() && pvpManager.isPvPModeEnabled()
                && pvpManager.isInPvP(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!pvpManager.isInPvP(event.getPlayer())) {
            return;
        }
        Set<String> blocked = settings.getBlockedContainers();
        if (blocked.isEmpty()) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        String typeName = block.getType().name();
        if (blocked.contains(typeName) || isShulkerBox(block.getType(), blocked)) {
            event.setCancelled(true);
            String message = Utils.color(messages.getContainerBlocked());
            if (!message.isEmpty()) {
                event.getPlayer().sendMessage(message);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!pvpManager.isInPvP(event.getPlayer())) {
            return;
        }
        Set<String> blocked = settings.getBlockedBlocks();
        if (blocked.isEmpty()) {
            return;
        }
        String typeName = event.getBlock().getType().name();
        if (!blocked.contains(typeName)) {
            return;
        }
        event.setCancelled(true);
        BlockedBlocksNotifications notifications = settings.getBlockedBlocksNotifications();
        if (notifications.isChatMessage()) {
            String message = Utils.color(messages.getBlockPlaceBlocked());
            if (!message.isEmpty()) {
                event.getPlayer().sendMessage(message);
            }
        }
        if (notifications.isActionbarMessage()) {
            String message = Utils.color(messages.getBlockPlaceBlockedActionbar());
            if (!message.isEmpty()) {
                ActionBar.sendAction(event.getPlayer(), message);
            }
        }
        if (notifications.isTitleMessage()) {
            sendBlockedTitle(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (!pvpManager.isInPvP(player)) {
            return;
        }
        Set<String> blocked = settings.getDisableInPvpModeItems();
        if (blocked.isEmpty()) {
            return;
        }
        if (isEquipAction(event, blocked)) {
            event.setCancelled(true);
            String message = Utils.color(messages.getEquipDisabledInPvp());
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (!pvpManager.isInPvP(player)) {
            return;
        }
        Set<String> blocked = settings.getDisableInPvpModeItems();
        if (blocked.isEmpty()) {
            return;
        }
        for (int slot : event.getRawSlots()) {
            if (isArmorSlot(slot, event.getView().getTopInventory().getType())) {
                ItemStack dragged = event.getOldCursor();
                if (dragged != null && blocked.contains(dragged.getType().name())) {
                    event.setCancelled(true);
                    String message = Utils.color(messages.getEquipDisabledInPvp());
                    if (!message.isEmpty()) {
                        player.sendMessage(message);
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!pvpManager.isInPvP(player)) {
            return;
        }
        Set<String> blocked = settings.getDisableInPvpModeItems();
        if (blocked.isEmpty()) {
            return;
        }
        ItemStack offHand = event.getOffHandItem();
        ItemStack mainHand = event.getMainHandItem();
        if ((offHand != null && blocked.contains(offHand.getType().name()))
                || (mainHand != null && blocked.contains(mainHand.getType().name()))) {
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEquip(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!pvpManager.isInPvP(player)) {
            return;
        }
        Set<String> blocked = settings.getDisableInPvpModeItems();
        if (blocked.isEmpty()) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (!blocked.contains(item.getType().name())) {
            return;
        }
        if (isArmorItem(item.getType())) {
            event.setCancelled(true);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            player.updateInventory();
            String message = Utils.color(messages.getEquipDisabledInPvp());
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
        }
    }

    private boolean isEquipAction(InventoryClickEvent event, Set<String> blocked) {
        int rawSlot = event.getRawSlot();
        boolean isArmorSlot = isArmorSlot(rawSlot, event.getView().getTopInventory().getType());
        InventoryAction action = event.getAction();

        if (isArmorSlot) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && blocked.contains(cursor.getType().name())) {
                if (action == InventoryAction.PLACE_ALL
                        || action == InventoryAction.PLACE_ONE
                        || action == InventoryAction.PLACE_SOME
                        || action == InventoryAction.SWAP_WITH_CURSOR) {
                    return true;
                }
            }
            if (event.getClick().isKeyboardClick()) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0) {
                    ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(hotbarSlot);
                    if (hotbarItem != null && blocked.contains(hotbarItem.getType().name())) {
                        return true;
                    }
                }
            }
        }

        if (event.isShiftClick()) {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem != null && blocked.contains(currentItem.getType().name())) {
                if (!isArmorSlot && isArmorItem(currentItem.getType())) {
                    if (getArmorSlotForItem(currentItem.getType(), (Player) event.getWhoClicked()) != -1) {
                        return true;
                    }
                }
            }
        }

        if (!isArmorSlot && event.getClick().isKeyboardClick()) {
            int hotbarSlot = event.getHotbarButton();
            if (hotbarSlot >= 0) {
                if (isArmorSlot(rawSlot, event.getView().getTopInventory().getType())) {
                    ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(hotbarSlot);
                    if (hotbarItem != null && blocked.contains(hotbarItem.getType().name())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isArmorSlot(int rawSlot, InventoryType topType) {
        if (topType == InventoryType.CRAFTING || topType == InventoryType.PLAYER) {
            return rawSlot >= 5 && rawSlot <= 8;
        }
        return false;
    }

    private boolean isArmorItem(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS")
                || material == Material.ELYTRA
                || name.equals("CARVED_PUMPKIN")
                || name.endsWith("_HEAD")
                || name.endsWith("_SKULL");
    }

    private int getArmorSlotForItem(Material material, Player player) {
        String name = material.name();
        if ((name.endsWith("_HELMET") || name.equals("CARVED_PUMPKIN") || name.endsWith("_HEAD")
                || name.endsWith("_SKULL"))
                && isEmpty(player.getInventory().getHelmet())) {
            return 5;
        }
        if ((name.endsWith("_CHESTPLATE") || material == Material.ELYTRA)
                && isEmpty(player.getInventory().getChestplate())) {
            return 6;
        }
        if (name.endsWith("_LEGGINGS") && isEmpty(player.getInventory().getLeggings())) {
            return 7;
        }
        if (name.endsWith("_BOOTS") && isEmpty(player.getInventory().getBoots())) {
            return 8;
        }
        return -1;
    }

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private void sendBlockedTitle(Player player) {
        String title = messages.getBlockPlaceBlockedTitle();
        String subtitle = messages.getBlockPlaceBlockedSubtitle();
        title = title.isEmpty() ? null : Utils.color(title);
        subtitle = subtitle.isEmpty() ? null : Utils.color(subtitle);
        if (title == null && subtitle == null) {
            return;
        }
        if (VersionUtils.isVersion(11)) {
            player.sendTitle(title, subtitle, 5, 20, 5);
        } else {
            player.sendTitle(title, subtitle);
        }
    }

    private boolean isShulkerBox(Material material, Set<String> blocked) {
        if (!blocked.contains("SHULKER_BOX")) {
            return false;
        }
        return material.name().endsWith("SHULKER_BOX");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCombust(EntityCombustByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player target = (Player) event.getEntity();
        Player damager = getDamager(event.getCombuster());
        pvpManager.playerDamagedByPlayer(damager, target);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent event) {
        if (VersionUtils.isVersion(14) && event.getProjectile() instanceof Firework
                && event.getEntity().getType() == EntityType.PLAYER) {
            event.getProjectile().setMetadata(META_KEY,
                    new FixedMetadataValue(plugin, event.getEntity().getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        if (e.getPotion() != null && e.getPotion().getShooter() instanceof Player) {
            Player shooter = (Player) e.getPotion().getShooter();
            for (LivingEntity en : e.getAffectedEntities()) {
                if (en.getType() == EntityType.PLAYER && en != shooter) {
                    for (PotionEffect ef : e.getPotion().getEffects()) {
                        if (ef.getType().equals(PotionEffectType.POISON)) {
                            pvpManager.playerDamagedByPlayer(shooter, (Player) en);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent ev) {
        if (settings.isDisableTeleportsInPvp() && pvpManager.isInPvP(ev.getPlayer())) {
            if (allowedTeleports.containsKey(ev.getPlayer())) {
                return;
            }
            if ((VersionUtils.isVersion(9) && ev.getCause() == TeleportCause.CHORUS_FRUIT)
                    || ev.getCause() == TeleportCause.ENDER_PEARL) {
                allowedTeleports.put(ev.getPlayer(), new AtomicInteger(0));
                return;
            }
            if (ev.getFrom().getWorld() != ev.getTo().getWorld()) {
                ev.setCancelled(true);
                return;
            }
            if (ev.getFrom().distanceSquared(ev.getTo()) > 100) {
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (settings.isDisableCommandsInPvp() && pvpManager.isInPvP(e.getPlayer())) {
            String fullCommand = e.getMessage().split(" ")[0].replaceFirst("/", "").toLowerCase();

            String command = fullCommand.contains(":")
                    ? fullCommand.split(":")[1]
                    : fullCommand;

            if (pvpManager.isCommandWhiteListed(command) || pvpManager.isCommandWhiteListed(fullCommand)) {
                return;
            }

            e.setCancelled(true);
            String message = Utils.color(messages.getCommandsDisabled());
            if (!message.isEmpty()) {
                e.getPlayer().sendMessage(Utils.replaceTime(message, pvpManager.getTimeRemainingInPvP(e.getPlayer())));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        if (!pvpManager.isInPvP(player)) return;

        if (event.getInventory().getType() != InventoryType.PLAYER
                && event.getInventory().getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
            String message = Utils.color(messages.getCommandsDisabled());
            if (!message.isEmpty()) {
                player.sendMessage(Utils.replaceTime(message, pvpManager.getTimeRemainingInPvP(player)));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        Player player = e.getPlayer();

        if (plugin.isDisabling()) {
            if (pvpManager.isInPvP(player) || pvpManager.isInSilentPvP(player)) {
                pvpManager.stopPvPSilentWithEvent(player, StopReason.SERVER_SHUTDOWN);
            }
            return;
        }

        if (pvpManager.isInSilentPvP(player)) {
            pvpManager.stopPvPSilentWithEvent(player, StopReason.KICK);
            return;
        }

        if (!pvpManager.isInPvP(player)) {
            return;
        }

        int timeRemaining = pvpManager.getTimeRemainingInPvP(player);
        pvpManager.stopPvPSilentWithEvent(player, StopReason.KICK);

        if (settings.getKickMessages().isEmpty()) {
            kickedInPvp(player, timeRemaining);
            return;
        }
        if (e.getReason() == null) {
            return;
        }
        String reason = ChatColor.stripColor(e.getReason().toLowerCase());
        for (String killReason : settings.getKickMessages()) {
            if (reason.contains(killReason.toLowerCase())) {
                kickedInPvp(player, timeRemaining);
                return;
            }
        }
    }

    private void kickedInPvp(Player player, int timeRemaining) {
        if (settings.isKillOnKick()) {
            Bukkit.getPluginManager()
                    .callEvent(new PvpPlayerKilledEvent(player, KillReason.KICKED_IN_PVP, timeRemaining));
            player.setHealth(0);
            sendLeavedInPvpMessage(player);
        }
        if (settings.isRunCommandsOnKick()) {
            runCommands(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        allowedTeleports.remove(e.getPlayer());
        if (settings.isHideLeaveMessage()) {
            e.setQuitMessage(null);
        }

        if (plugin.isDisabling()) {
            if (pvpManager.isInPvP(e.getPlayer()) || pvpManager.isInSilentPvP(e.getPlayer())) {
                pvpManager.stopPvPSilentWithEvent(e.getPlayer(), StopReason.SERVER_SHUTDOWN);
            }
            return;
        }

        if (pvpManager.isInPvP(e.getPlayer())) {
            int timeRemaining = pvpManager.getTimeRemainingInPvP(e.getPlayer());
            pvpManager.stopPvPSilentWithEvent(e.getPlayer(), StopReason.QUIT);
            if (settings.isKillOnLeave()) {
                sendLeavedInPvpMessage(e.getPlayer());
                Bukkit.getPluginManager()
                        .callEvent(new PvpPlayerKilledEvent(e.getPlayer(), KillReason.QUIT_IN_PVP, timeRemaining));
                e.getPlayer().setHealth(0);
            }
            runCommands(e.getPlayer());
        } else if (pvpManager.isInSilentPvP(e.getPlayer())) {
            pvpManager.stopPvPSilentWithEvent(e.getPlayer(), StopReason.QUIT);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent e) {
        if (settings.isHideDeathMessage()) {
            e.setDeathMessage(null);
        }
        if (pvpManager.isInSilentPvP(e.getEntity()) || pvpManager.isInPvP(e.getEntity())) {
            pvpManager.stopPvPSilentWithEvent(e.getEntity(), StopReason.DEATH);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        if (settings.isHideJoinMessage()) {
            e.setJoinMessage(null);
        }
    }

    private void sendLeavedInPvpMessage(Player p) {
        String message = Utils.color(messages.getPvpLeaved()).replace("%player%", p.getName());
        if (!message.isEmpty()) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.sendMessage(message);
            }
        }
    }

    private void runCommands(Player leaved) {
        if (!settings.getCommandsOnLeave().isEmpty()) {
            settings.getCommandsOnLeave().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    Utils.color(command).replace("%player%", leaved.getName())));
        }
    }

    private Player getDamager(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile proj = (Projectile) damager;
            if (proj.getShooter() instanceof Player) {
                return (Player) proj.getShooter();
            }
        } else if (damager instanceof TNTPrimed) {
            TNTPrimed tntPrimed = (TNTPrimed) damager;
            return getDamager(tntPrimed.getSource());
        } else if (VersionUtils.isVersion(9) && damager instanceof AreaEffectCloud) {
            AreaEffectCloud aec = (AreaEffectCloud) damager;
            if (aec.getSource() instanceof Player) {
                return (Player) aec.getSource();
            }
        } else if (VersionUtils.isVersion(14) && damager instanceof Firework) {
            if (damager.hasMetadata(META_KEY)) {
                MetadataValue metadata = null;
                for (MetadataValue metadataValue : damager.getMetadata(META_KEY)) {
                    if (metadataValue.getOwningPlugin() == plugin) {
                        metadata = metadataValue;
                        break;
                    }
                }
                if (metadata != null) {
                    damager.removeMetadata(META_KEY, plugin);
                    return Bukkit.getPlayer((UUID) metadata.value());
                }
            }
        }
        return null;
    }
}