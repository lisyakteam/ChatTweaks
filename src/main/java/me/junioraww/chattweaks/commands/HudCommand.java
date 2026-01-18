package me.junioraww.chattweaks.commands;

import me.junioraww.chattweaks.Main;
import me.junioraww.chattweaks.modules.Sidebar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HudCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) return true;

    String path = "player-settings." + player.getName().toLowerCase() + ".no-sidebar";
    boolean newState = !Main.getSettings().getBoolean(path, false);

    Main.getSettings().set(path, newState);
    Main.saveSettings();

    Sidebar.updateVisibility(player, newState);

    if (!newState) {
      player.sendRichMessage("<green>✔ HUD включен!");
    } else {
      player.sendRichMessage("<yellow>➜ HUD отключен!");
    }
    return true;
  }
}