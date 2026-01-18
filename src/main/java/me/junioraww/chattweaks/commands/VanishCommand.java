package me.junioraww.chattweaks.commands;

import me.junioraww.chattweaks.listeners.ChatEvents;
import me.junioraww.chattweaks.Main;
import me.junioraww.chattweaks.modules.Tab;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {

  private final JavaPlugin plugin;

  private final Set<UUID> vanishedPlayers = new HashSet<>();

  public int online() {
    int count = 0;
    for (var player : Bukkit.getOnlinePlayers()) {
      if (!vanishedPlayers.contains(player.getUniqueId())) count++;
    }
    return count;
  }

  public VanishCommand(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (sender instanceof Player player && sender.hasPermission("tweaks.vanish")) {
      UUID uuid = player.getUniqueId();

      if (vanishedPlayers.contains(uuid)) {
        vanishedPlayers.remove(uuid);
        player.setGameMode(GameMode.SURVIVAL);

        ChatEvents chatEvents = Main.getInstance().getChatEvents();
        var message = ChatEvents.joinMessage(player.getName());
        chatEvents.getHistory().add(message, message);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
          onlinePlayer.showPlayer(plugin, player);
          onlinePlayer.sendMessage(message);
        }

        Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
        player.sendMessage("§aВы теперь видимы для всех!");
      } else {
        vanishedPlayers.add(uuid);
        player.setGameMode(GameMode.SPECTATOR);

        ChatEvents chatEvents = Main.getInstance().getChatEvents();
        var message = ChatEvents.quitMessage(player.getName());
        chatEvents.getHistory().add(message, message);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
          onlinePlayer.sendMessage(message);
          if (!onlinePlayer.equals(player)) {
            onlinePlayer.hidePlayer(plugin, player);
          }
        }

        Bukkit.getOnlinePlayers().forEach(Tab::sendFancyTab);
        player.sendMessage("§cВы полностью скрыты (World + TAB)!");
      }
    }
    return true;
  }

  public Set<UUID> getVanishedPlayers() {
    return vanishedPlayers;
  }
}