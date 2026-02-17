package ru.leymooo.antirelog.config;

import ru.leymooo.annotatedyaml.Annotations.*;
import ru.leymooo.annotatedyaml.ConfigurationSection;

public class CooldownNotificationEntry implements ConfigurationSection {

    @Comment("Показывать ли сообщение в чат когда кулдаун активен")
    @Key("chat-message")
    private boolean chatMessage;

    @Comment("Показывать ли сообщение в actionbar при выборе предмета на кулдауне")
    @Key("actionbar-message")
    private boolean actionbarMessage;

    @Comment("Показывать ли сообщение когда предмет заблокирован в PvP")
    @Key("blocked-message")
    private boolean blockedMessage;

    public CooldownNotificationEntry() {
        this.chatMessage = true;
        this.actionbarMessage = true;
        this.blockedMessage = true;
    }

    public CooldownNotificationEntry(boolean chatMessage, boolean actionbarMessage, boolean blockedMessage) {
        this.chatMessage = chatMessage;
        this.actionbarMessage = actionbarMessage;
        this.blockedMessage = blockedMessage;
    }

    public boolean isChatMessage() {
        return chatMessage;
    }

    public boolean isActionbarMessage() {
        return actionbarMessage;
    }

    public boolean isBlockedMessage() {
        return blockedMessage;
    }

    @Override
    public String toString() {
        return "CooldownNotificationEntry{" +
                "chatMessage=" + chatMessage +
                ", actionbarMessage=" + actionbarMessage +
                ", blockedMessage=" + blockedMessage +
                '}';
    }
}