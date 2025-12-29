package me.junioraww.chatTweaks;

import me.junioraww.chatTweaks.commands.Animation;
import me.junioraww.chatTweaks.commands.HudCommand;
import me.junioraww.chatTweaks.modules.Ping;
import me.junioraww.chatTweaks.modules.Tab;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin implements Listener {
    private static YamlConfiguration settings;
    private static File settingsFile;
    public static YamlConfiguration getSettings() {
        return settings;
    }
    private static Scoreboard scoreboard;

    private static Main main;
    public static Main get() {
      return main;
    }

    public static Scoreboard getScoreboard() {
      return scoreboard;
    }

    public static boolean saveSettings() {
        try {
          settings.save(settingsFile);
        } catch (IOException e) {
          return false;
        }
        return true;
    }

    @Override
    public void onEnable() {
      main = this;
      settings = new YamlConfiguration();
      settingsFile = new File(getDataFolder(), "settings.yml");

      if (!settingsFile.exists()) {
        settingsFile.getParentFile().mkdirs();
        try { settingsFile.createNewFile(); } catch (IOException e) { throw new RuntimeException(e); }
      }
      try { settings.load(settingsFile); } catch (Exception e) { throw new RuntimeException(e); }

      ScoreboardManager manager = Bukkit.getScoreboardManager();
      scoreboard = manager.getNewScoreboard();

      Tab.init(settings);

      Ping.setupScoreboard();
      getServer().getPluginManager().registerEvents(new Ping(), this);
      Bukkit.getScheduler().runTaskTimer(this, Ping::updatePings, 40L, 40L);

      getServer().getPluginManager().registerEvents(new ChatEvents(this), this);
      getServer().getPluginManager().registerEvents(this, this);
      Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
      Bukkit.getOnlinePlayers().forEach(Tab::setSidebar);

      Bukkit.getOnlinePlayers().forEach(p -> {
        p.setScoreboard(Main.getScoreboard());
      });

      this.getCommand("hud").setExecutor(new HudCommand());
      this.getCommand("test").setExecutor(new Animation());
    }

    @Override
    public void onDisable() {
      main = null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
      Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
      Bukkit.getOnlinePlayers().forEach(Tab::setSidebar);
      event.getPlayer().setScoreboard(scoreboard);
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent event) {
      Bukkit.getScheduler().runTask(this, task -> {
        Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
        Bukkit.getOnlinePlayers().forEach(Tab::setSidebar);
      });
    }
}
