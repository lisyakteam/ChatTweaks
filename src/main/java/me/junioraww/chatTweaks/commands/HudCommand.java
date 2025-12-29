package me.junioraww.chatTweaks.commands;

import me.junioraww.chatTweaks.Main;
import me.junioraww.chatTweaks.modules.Tab;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HudCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (sender instanceof Player player) {
      if (args.length > 0) {

      } else {
        var key = sender.getName().toLowerCase() + ".no-sidebar";
        var inverse = Main.getSettings().contains(key) && !Main.getSettings().getBoolean(key);
        Main.getSettings().set(key, inverse);
        Main.saveSettings();

        if (inverse) {
          player.sendRichMessage("<green>HUD включен!");
          Tab.setSidebar(player);
        } else {
          player.sendRichMessage("<yellow>HUD отключен!");
          player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
      }
    }

    return true;
  }

}
