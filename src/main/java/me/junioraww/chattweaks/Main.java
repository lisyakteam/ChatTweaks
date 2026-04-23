package me.junioraww.chattweaks;

import me.junioraww.chattweaks.commands.*;
import me.junioraww.chattweaks.listeners.AnvilEvents;
import me.junioraww.chattweaks.listeners.ChatEvents;
import me.junioraww.chattweaks.modules.*;
import me.junioraww.chattweaks.utils.EmojiProcessor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin implements Listener {
  private static YamlConfiguration settings;
  private static File settingsFile;
  private static Main instance;
  private ChatEvents chatEvents;
  private VanishCommand vanishCommand;

  public static Main getInstance() {
    return instance;
  }

  public static YamlConfiguration getSettings() {
    return settings;
  }

  public ChatEvents getChatEvents() {
    return chatEvents;
  }

  public VanishCommand getVanishCommand() {
    return vanishCommand;
  }

  @Override
  public void onEnable() {
    instance = this;
    saveDefaultConfig();
    loadSettings();

    chatEvents = new ChatEvents(this);
    vanishCommand = new VanishCommand(this);

    Tab.init(settings);
    Sidebar.init(settings);
    EmojiProcessor.load();

    AutoMessages.init(this);

    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(chatEvents, this);
    getServer().getPluginManager().registerEvents(new CustomTriggerHandle(), this);
    getServer().getPluginManager().registerEvents(new AnvilEvents(), this);

    Bukkit.getScheduler().runTaskTimer(this, Ping::updatePings, 40L, 40L);

    getCommand("board").setExecutor(new HudCommand());
    ColorsMenu colorsMenu = new ColorsMenu(this);
    //getCommand("me").setExecutor(colorsMenu);
    getServer().getPluginManager().registerEvents(colorsMenu, this);

    MuteCommand muteCmd = new MuteCommand();
    getCommand("mute").setExecutor(muteCmd);
    getCommand("mute").setTabCompleter(muteCmd);

    DMCommand dmCmd = new DMCommand();
    getCommand("tell").setExecutor(dmCmd);
    getCommand("tell").setTabCompleter(dmCmd);

    getCommand("vanish").setExecutor(vanishCommand);
    getServer().getPluginManager().registerEvents(new VanishListener(this, vanishCommand), this);

    for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
      Sidebar.setupPlayerScoreboard(p);
      Tab.sendFancyTab(p);
    }
  }

  @Override
  public void onDisable() {

  }

  private void loadSettings() {
    settingsFile = new File(getDataFolder(), "config.yml");

    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }

    if (!settingsFile.exists()) {
      saveResource("config.yml", false);
    }

    settings = YamlConfiguration.loadConfiguration(settingsFile);
  }

  public static boolean saveSettings() {
    try {
      settings.save(settingsFile);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /* there is 1 more listener in ChatEvents */
  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player p = event.getPlayer();
    String path = "player-settings." + p.getName().toLowerCase() + ".no-sidebar";

    if (!Main.getSettings().getBoolean(path, false)) {
      Sidebar.setupPlayerScoreboard(p);
    }

    Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
    //ColorsMenu.updatePlayerDisplay(p);
  }

  @EventHandler
  public void onLeft(PlayerQuitEvent event) {
    Sidebar.clearPlayerCache(event.getPlayer());
    Bukkit.getScheduler().runTask(this, () -> {
      Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
    });
  }
}