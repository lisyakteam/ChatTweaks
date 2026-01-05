package me.junioraww.chattweaks;

import me.junioraww.chattweaks.commands.ColorsMenu;
import me.junioraww.chattweaks.commands.HudCommand;
import me.junioraww.chattweaks.commands.VanishCommand;
import me.junioraww.chattweaks.modules.AutoMessages;
import me.junioraww.chattweaks.modules.Ping;
import me.junioraww.chattweaks.modules.Sidebar;
import me.junioraww.chattweaks.modules.Tab;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

    AutoMessages.init(this);

    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(chatEvents, this);

    getServer().getPluginManager().registerEvents(new Ping(), this);
    Bukkit.getScheduler().runTaskTimer(this, Ping::updatePings, 40L, 40L);

    getCommand("hud").setExecutor(new HudCommand());
    ColorsMenu colorsMenu = new ColorsMenu(this);
    getCommand("me").setExecutor(colorsMenu);
    getServer().getPluginManager().registerEvents(colorsMenu, this);

    getCommand("vanish").setExecutor(vanishCommand);
    getServer().getPluginManager().registerEvents(new VanishListener(this, vanishCommand), this);

    for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
      Sidebar.setupPlayerScoreboard(p);
      Tab.sendFancyTab(p);
    }
  }

  private void loadSettings() {
    settingsFile = new File(getDataFolder(), "config.yml");
    if (!settingsFile.exists()) {
      throw new RuntimeException("Create ChatTweaks/config.yml");
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

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player p = event.getPlayer();
    String path = "player-settings." + p.getName().toLowerCase() + ".no-sidebar";

    if (!Main.getSettings().getBoolean(path, false)) {
      Sidebar.setupPlayerScoreboard(p);
    }

    Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
    ColorsMenu.updatePlayerDisplay(p);
  }

  @EventHandler
  public void onLeft(PlayerQuitEvent event) {
    Sidebar.clearPlayerCache(event.getPlayer());
    Bukkit.getScheduler().runTask(this, () -> {
      Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
    });
  }
}