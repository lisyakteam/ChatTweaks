package me.junioraww.chattweaks.commands;

import me.junioraww.chattweaks.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DMCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (args.length < 2) {
      sender.sendMessage("§cИспользование: /" + label + " [игрок] [сообщение]");
      return true;
    }

    Player target = Bukkit.getPlayer(args[0]);

    // Проверка на ваниш
    boolean isVanished = target != null && Main.getInstance().getVanishCommand().getVanishedPlayers().contains(target.getUniqueId());
    if (target == null || (isVanished && !sender.hasPermission("tweaks.vanish"))) {
      sender.sendMessage("§cИгрок " + args[0] + " не найден.");
      return true;
    }

    StringBuilder msg = new StringBuilder();
    for (int i = 1; i < args.length; i++) msg.append(args[i]).append(" ");
    String message = msg.toString().trim();

    sender.sendMessage("§7[Я -> " + target.getName() + "] §f" + message);
    target.sendMessage("§7[" + sender.getName() + " -> Я] §f" + message);

    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
    if (args.length == 1) {
      return Bukkit.getOnlinePlayers().stream()
              .filter(p -> !Main.getInstance().getVanishCommand().getVanishedPlayers().contains(p.getUniqueId()) || sender.hasPermission("tweaks.vanish"))
              .map(Player::getName)
              .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }
}