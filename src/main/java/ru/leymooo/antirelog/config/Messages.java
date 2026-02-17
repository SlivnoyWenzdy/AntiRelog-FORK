package ru.leymooo.antirelog.config;

import ru.leymooo.annotatedyaml.Annotations.*;
import ru.leymooo.annotatedyaml.ConfigurationSection;

@Comment("Для того, чтобы отключить сообщение, оставьте его пустым")
public class Messages implements ConfigurationSection {
    @Key("pvp-started")
    private String pvpStarted = "&bВы начали &e&lPVP&b!";
    @Key("pvp-started-title")
    private String pvpStartedTitle = "&bAntiRelog";
    @Key("pvp-started-subtitle")
    private String pvpStartedSubtitle = "Вы вошли в режим &ePVP&a!";
    @Key("pvp-stopped")
    private String pvpStopped = "&e&lPVP &bокончено";
    @Key("pvp-stopped-title")
    private String pvpStoppedTitle = "&bAntiRelog";
    @Key("pvp-stopped-subtitle")
    private String pvpStoppedSubtitle = "Вы вышли из режима &ePVP&a!";
    @Key("pvp-stopped-actionbar")
    private String pvpStoppedActionbar = "&e&lPVP &aокончено, Вы снова можете использовать команды и выходить из игры!";
    @Key("in-pvp-bossbar")
    private String inPvpBossbar = "&r&lРежим &c&lPVP &r&l- &a&l%time% &r&l%formated-sec%.";
    @Key("in-pvp-actionbar")
    private String inPvpActionbar = "&r&lРежим &c&lPVP&r&l, не выходите из игры &a&l%time% &r&l%formated-sec%.";
    @Key("pvp-leaved")
    private String pvpLeaved = "&aИгрок &c&l%player% &aпокинул игру во время &b&lПВП&a и был наказан.";
    @Key("commands-disabled")
    private String commandsDisabled = "&b&lВы не можете использовать команды в &e&lPvP&b&l. &b&lПодождите &a&l%time% &b&l%formated-sec%.";
    @Key("item-cooldown")
    private String itemCooldown = "&b&lВы сможете использовать этот предмет через &a&l%time% &b&l%formated-sec%.";
    @Key("item-disabled-in-pvp")
    private String itemDisabledInPvp = "&b&lВы не можете использовать этот предмет в &e&lPVP &b&lрежиме";
    @Key("totem-cooldown")
    private String totemCooldown = "&b&lТотем небыл использован, т.к был недавно использован. Тотем будет доступен через &a&l%time% &b&l%formated-sec%.";
    @Key("totem-disabled-in-pvp")
    private String totemDisabledInPvp = "&b&lТотем небыл использован, т.к он отключен в &e&lPVP &b&lрежиме";
    @Key("pvp-started-with-powerups")
    @Comment("Данное сообщение будет появляться только тогда, когда настроена функция 'commands-on-powerups-disable'")
    private String pvpStartedWithPowerups = "&c&lВы начали пвп с включеным GM/FLY/и тд и за это получили негативный эффект";
    @Key("container-blocked")
    private String containerBlocked = "&c&lВы не можете открыть это во время &e&lPVP&c&l!";
    @Key("block-place-blocked")
    private String blockPlaceBlocked = "&c&lВы не можете ставить этот блок во время &e&lPVP&c&l!";
    @Key("block-place-blocked-actionbar")
    private String blockPlaceBlockedActionbar = "&c⚠ &fЭтот блок нельзя ставить в &ePVP";
    @Key("block-place-blocked-title")
    private String blockPlaceBlockedTitle = "";
    @Key("block-place-blocked-subtitle")
    private String blockPlaceBlockedSubtitle = "&cЭтот блок нельзя ставить в PVP!";
    @Key("potion-cooldown")
    private String potionCooldown = "&b&lВы сможете использовать это зелье через &a&l%time% &b&l%formated-sec%.";
    @Key("potion-disabled-in-pvp")
    private String potionDisabledInPvp = "&b&lВы не можете использовать это зелье в &e&lPVP &b&lрежиме";
    @Key("item-cooldown-actionbar")
    private String itemCooldownActionbar = "&c⚠ &fПредмет будет доступен через &c%time% &f%formated-sec%.";
    @Key("potion-cooldown-actionbar")
    private String potionCooldownActionbar = "&c⚠ &fЗелье будет доступно через &c%time% &f%formated-sec%.";
    @Key("item-disabled-in-pvp-actionbar")
    private String itemDisabledInPvpActionbar = "&c⚠ &fЭтот предмет заблокирован в &ePVP";
    @Key("potion-disabled-in-pvp-actionbar")
    private String potionDisabledInPvpActionbar = "&c⚠ &fЭто зелье заблокировано в &ePVP";

    public String getPvpStarted() {
        return pvpStarted;
    }

    public String getPvpStartedTitle() {
        return pvpStartedTitle;
    }

    public String getPvpStartedSubtitle() {
        return pvpStartedSubtitle;
    }

    public String getPvpStopped() {
        return pvpStopped;
    }

    public String getPvpStoppedTitle() {
        return pvpStoppedTitle;
    }

    public String getPvpStoppedSubtitle() {
        return pvpStoppedSubtitle;
    }

    public String getPvpStoppedActionbar() {
        return pvpStoppedActionbar;
    }

    public String getInPvpBossbar() {
        return inPvpBossbar;
    }

    public String getInPvpActionbar() {
        return inPvpActionbar;
    }

    public String getPvpLeaved() {
        return pvpLeaved;
    }

    public String getCommandsDisabled() {
        return commandsDisabled;
    }

    public String getItemCooldown() {
        return itemCooldown;
    }

    public String getItemDisabledInPvp() {
        return itemDisabledInPvp;
    }

    public String getTotemCooldown() {
        return totemCooldown;
    }

    public String getTotemDisabledInPvp() {
        return totemDisabledInPvp;
    }

    public String getPvpStartedWithPowerups() {
        return pvpStartedWithPowerups;
    }

    public String getContainerBlocked() {
        return containerBlocked;
    }

    public String getBlockPlaceBlocked() {
        return blockPlaceBlocked;
    }

    public String getBlockPlaceBlockedActionbar() {
        return blockPlaceBlockedActionbar;
    }

    public String getBlockPlaceBlockedTitle() {
        return blockPlaceBlockedTitle;
    }

    public String getBlockPlaceBlockedSubtitle() {
        return blockPlaceBlockedSubtitle;
    }

    public String getPotionCooldown() {
        return potionCooldown;
    }

    public String getPotionDisabledInPvp() {
        return potionDisabledInPvp;
    }

    public String getItemCooldownActionbar() {
        return itemCooldownActionbar;
    }

    public String getPotionCooldownActionbar() {
        return potionCooldownActionbar;
    }

    public String getItemDisabledInPvpActionbar() {
        return itemDisabledInPvpActionbar;
    }

    public String getPotionDisabledInPvpActionbar() {
        return potionDisabledInPvpActionbar;
    }

    @Override
    public String toString() {
        return "Messages{" +
                "pvpStarted='" + pvpStarted + '\'' +
                ", pvpStartedTitle='" + pvpStartedTitle + '\'' +
                ", pvpStartedSubtitle='" + pvpStartedSubtitle + '\'' +
                ", pvpStopped='" + pvpStopped + '\'' +
                ", pvpStoppedTitle='" + pvpStoppedTitle + '\'' +
                ", pvpStoppedSubtitle='" + pvpStoppedSubtitle + '\'' +
                ", pvpStoppedActionbar='" + pvpStoppedActionbar + '\'' +
                ", inPvpBossbar='" + inPvpBossbar + '\'' +
                ", inPvpActionbar='" + inPvpActionbar + '\'' +
                ", pvpLeaved='" + pvpLeaved + '\'' +
                ", commandsDisabled='" + commandsDisabled + '\'' +
                ", itemCooldown='" + itemCooldown + '\'' +
                ", itemDisabledInPvp='" + itemDisabledInPvp + '\'' +
                ", totemCooldown='" + totemCooldown + '\'' +
                ", totemDisabledInPvp='" + totemDisabledInPvp + '\'' +
                ", pvpStartedWithPowerups='" + pvpStartedWithPowerups + '\'' +
                ", containerBlocked='" + containerBlocked + '\'' +
                ", blockPlaceBlocked='" + blockPlaceBlocked + '\'' +
                ", blockPlaceBlockedActionbar='" + blockPlaceBlockedActionbar + '\'' +
                ", blockPlaceBlockedTitle='" + blockPlaceBlockedTitle + '\'' +
                ", blockPlaceBlockedSubtitle='" + blockPlaceBlockedSubtitle + '\'' +
                ", potionCooldown='" + potionCooldown + '\'' +
                ", potionDisabledInPvp='" + potionDisabledInPvp + '\'' +
                '}';
    }
}