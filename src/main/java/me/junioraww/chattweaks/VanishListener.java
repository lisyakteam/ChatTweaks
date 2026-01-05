package me.junioraww.chattweaks;

import me.junioraww.chattweaks.commands.VanishCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class VanishListener implements Listener {

  private final JavaPlugin plugin;
  private final VanishCommand vanishCommand;

  public VanishListener(JavaPlugin plugin, VanishCommand vanishCommand) {
    this.plugin = plugin;
    this.vanishCommand = vanishCommand;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player joinedPlayer = event.getPlayer();

    for (org.bukkit.entity.Player onlinePlayer : org.bukkit.Bukkit.getOnlinePlayers()) {
      if (vanishCommand.getVanishedPlayers().contains(onlinePlayer.getUniqueId())) {
        joinedPlayer.hidePlayer(plugin, onlinePlayer);
      }
    }
  }
}