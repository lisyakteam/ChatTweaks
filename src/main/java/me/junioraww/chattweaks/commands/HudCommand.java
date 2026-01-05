package me.junioraww.chattweaks.commands;

import me.junioraww.chattweaks.Main;
import me.junioraww.chattweaks.modules.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HudCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) return true;

    // Ключ теперь защищен приставкой player-settings.
    String playerName = player.getName().toLowerCase();
    String path = "player-settings." + playerName + ".no-sidebar";

    boolean isHidden = Main.getSettings().getBoolean(path, false);
    boolean newState = !isHidden;

    Main.getSettings().set(path, newState);
    Main.saveSettings();

    if (!newState) {
      player.sendRichMessage("<green>✔ HUD включен!");
      Sidebar.setupPlayerScoreboard(player);
    } else {
      player.sendRichMessage("<yellow>➜ HUD отключен!");
      player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      Sidebar.clearPlayerCache(player);
    }

    return true;
  }
}