package ru.leymooo.antirelog.config;

import ru.leymooo.annotatedyaml.Annotations.*;
import ru.leymooo.annotatedyaml.ConfigurationSection;

@Comment({"Настройки уведомлений для каждого типа кулдауна.",
        "chat-message - показывать ли сообщение в чат когда кулдаун активен",
        "actionbar-message - показывать ли сообщение в actionbar при выборе предмета на кулдауне",
        "blocked-message - показывать ли сообщение когда предмет заблокирован в PvP"})
public class CooldownNotifications implements ConfigurationSection {

    @Comment("Уведомления для обычных золотых яблок")
    @Key("golden-apple")
    private CooldownNotificationEntry goldenApple = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для зачарованных золотых яблок")
    @Key("enchanted-golden-apple")
    private CooldownNotificationEntry enchantedGoldenApple = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для жемчугов края")
    @Key("ender-pearl")
    private CooldownNotificationEntry enderPearl = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для корусов")
    @Key("chorus")
    private CooldownNotificationEntry chorus = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для фейерверков")
    @Key("firework")
    private CooldownNotificationEntry firework = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для тотемов бессмертия")
    @Key("totem")
    private CooldownNotificationEntry totem = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для зелий исцеления")
    @Key("healing-potion")
    private CooldownNotificationEntry healingPotion = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для зелий регенерации")
    @Key("regeneration-potion")
    private CooldownNotificationEntry regenerationPotion = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для зелий силы")
    @Key("strength-potion")
    private CooldownNotificationEntry strengthPotion = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для зелий скорости")
    @Key("speed-potion")
    private CooldownNotificationEntry speedPotion = new CooldownNotificationEntry(true, true, true);

    @Comment("Уведомления для пузырьков опыта")
    @Key("experience-bottle")
    private CooldownNotificationEntry experienceBottle = new CooldownNotificationEntry(true, true, true);

    public CooldownNotificationEntry getGoldenApple() {
        return goldenApple;
    }

    public CooldownNotificationEntry getEnchantedGoldenApple() {
        return enchantedGoldenApple;
    }

    public CooldownNotificationEntry getEnderPearl() {
        return enderPearl;
    }

    public CooldownNotificationEntry getChorus() {
        return chorus;
    }

    public CooldownNotificationEntry getFirework() {
        return firework;
    }

    public CooldownNotificationEntry getTotem() {
        return totem;
    }

    public CooldownNotificationEntry getHealingPotion() {
        return healingPotion;
    }

    public CooldownNotificationEntry getRegenerationPotion() {
        return regenerationPotion;
    }

    public CooldownNotificationEntry getStrengthPotion() {
        return strengthPotion;
    }

    public CooldownNotificationEntry getSpeedPotion() {
        return speedPotion;
    }

    public CooldownNotificationEntry getExperienceBottle() {
        return experienceBottle;
    }

    @Override
    public String toString() {
        return "CooldownNotifications{" +
                "goldenApple=" + goldenApple +
                ", enchantedGoldenApple=" + enchantedGoldenApple +
                ", enderPearl=" + enderPearl +
                ", chorus=" + chorus +
                ", firework=" + firework +
                ", totem=" + totem +
                ", healingPotion=" + healingPotion +
                ", regenerationPotion=" + regenerationPotion +
                ", strengthPotion=" + strengthPotion +
                ", speedPotion=" + speedPotion +
                ", experienceBottle=" + experienceBottle +
                '}';
    }
}