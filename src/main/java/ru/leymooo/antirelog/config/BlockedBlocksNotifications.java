package ru.leymooo.antirelog.config;

import ru.leymooo.annotatedyaml.Annotations.*;
import ru.leymooo.annotatedyaml.ConfigurationSection;

@Comment({"Настройки уведомлений для заблокированных блоков во время PvP.",
        "chat-message - показывать ли сообщение в чат когда блок заблокирован",
        "actionbar-message - показывать ли сообщение в actionbar когда блок заблокирован",
        "title-message - показывать ли title когда блок заблокирован"})
public class BlockedBlocksNotifications implements ConfigurationSection {

    @Comment("Показывать ли сообщение в чат когда блок заблокирован")
    @Key("chat-message")
    private boolean chatMessage = true;

    @Comment("Показывать ли сообщение в actionbar когда блок заблокирован")
    @Key("actionbar-message")
    private boolean actionbarMessage = false;

    @Comment("Показывать ли title когда блок заблокирован")
    @Key("title-message")
    private boolean titleMessage = false;

    public boolean isChatMessage() {
        return chatMessage;
    }

    public boolean isActionbarMessage() {
        return actionbarMessage;
    }

    public boolean isTitleMessage() {
        return titleMessage;
    }

    @Override
    public String toString() {
        return "BlockedBlocksNotifications{" +
                "chatMessage=" + chatMessage +
                ", actionbarMessage=" + actionbarMessage +
                ", titleMessage=" + titleMessage +
                '}';
    }
}