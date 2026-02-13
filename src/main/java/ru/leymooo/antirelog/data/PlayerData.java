package ru.leymooo.antirelog.data;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import ru.leymooo.antirelog.config.Settings;
import ru.leymooo.antirelog.util.Utils;

public class PlayerData {

    private final BossBar bossBar;
    private final int time;
    private int delay;
    private boolean silent;
    private boolean bossBarVisible;

    public PlayerData(Settings settings, int time, Player player, boolean silent) {
        this.time = time;
        this.delay = time;
        this.silent = silent;
        this.bossBarVisible = true;
        if (!silent) {
            String title = Utils.color(settings.getMessages().getInPvpBossbar());
            if (!title.isEmpty() && time > 0) {
                String actualTitle = Utils.replaceTime(title, time);
                this.bossBar = Bukkit.createBossBar(actualTitle, BarColor.RED, BarStyle.SOLID);
                this.bossBar.setProgress(1.0);
                this.bossBar.addPlayer(player);
            } else if (!title.isEmpty() && time < 0) {
                String actualTitle = title.replace("%time%", "∞").replace("%formated-sec%", "");
                this.bossBar = Bukkit.createBossBar(Utils.color(actualTitle), BarColor.RED, BarStyle.SOLID);
                this.bossBar.setProgress(1.0);
                this.bossBar.addPlayer(player);
            } else {
                this.bossBar = null;
            }
        } else {
            this.bossBar = null;
        }
    }

    public void updateBossBar(Settings settings) {
        if (bossBar == null || !bossBarVisible) {
            return;
        }
        if (time < 0) {
            String title = Utils.color(settings.getMessages().getInPvpBossbar());
            String actualTitle = title.replace("%time%", "∞").replace("%formated-sec%", "");
            bossBar.setTitle(Utils.color(actualTitle));
            bossBar.setProgress(1.0);
            return;
        }
        String actualTitle = Utils.replaceTime(Utils.color(settings.getMessages().getInPvpBossbar()), delay);
        bossBar.setTitle(actualTitle);
        double progress = (1.0 / (double) time) * (double) delay;
        bossBar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
    }

    public void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void setBossBarVisible(boolean visible) {
        this.bossBarVisible = visible;
        if (bossBar != null) {
            bossBar.setVisible(visible);
        }
    }

    public boolean isBossBarVisible() {
        return bossBarVisible;
    }

    public int getTime() {
        return time;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isInfinite() {
        return time < 0;
    }
}