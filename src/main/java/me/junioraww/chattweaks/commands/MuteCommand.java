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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MuteCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (!sender.hasPermission("tweaks.mute")) {
      sender.sendMessage("§cУ вас нет прав!");
      return true;
    }

    if (args.length < 3) {
      sender.sendMessage("§cИспользование: /mute [игрок] [время] [причина]");
      sender.sendMessage("§7Пример: /mute JuniorAww 10m Спам");
      return true;
    }

    Player target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      sender.sendMessage("§cИгрок не найден.");
      return true;
    }

    long durationMs = parseTime(args[1]);
    if (durationMs <= 0) {
      sender.sendMessage("§cНеверный формат времени! Используйте: 30s, 10m, 1h, 1d");
      return true;
    }

    long expiration = System.currentTimeMillis() + durationMs;

    // Сбор причины
    StringBuilder reasonBuilder = new StringBuilder();
    for (int i = 2; i < args.length; i++) {
      reasonBuilder.append(args[i]).append(" ");
    }
    String reason = reasonBuilder.toString().trim();

    // Сохранение в конфиг
    String path = "mutes." + target.getUniqueId();
    Main.getSettings().set(path + ".end", expiration);
    Main.getSettings().set(path + ".reason", reason);
    Main.getSettings().set(path + ".name", target.getName()); // Для удобства в конфиге
    Main.saveSettings();

    sender.sendMessage("§aВы замутили " + target.getName() + " на " + args[1] + ". Причина: " + reason);
    target.sendMessage("§cВам ограничили чат на " + args[1] + ".");
    target.sendMessage("§cПричина: §f" + reason);

    return true;
  }

  private long parseTime(String input) {
    long totalMs = 0;
    // Исправленное регулярное выражение: ищем число и букву (s, m, h, d)
    Pattern p = Pattern.compile("(\\d+)([smhd])");
    Matcher m = p.matcher(input.toLowerCase());
    boolean found = false;

    while (m.find()) {
      found = true;
      long value = Long.parseLong(m.group(1));
      switch (m.group(2)) {
        case "s" -> totalMs += value * 1000L;
        case "m" -> totalMs += value * 60000L;
        case "h" -> totalMs += value * 3600000L;
        case "d" -> totalMs += value * 86400000L;
      }
    }
    return found ? totalMs : -1;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
    if (args.length == 1) {
      String input = args[0].toLowerCase();

      return Bukkit.getOnlinePlayers().stream()
              .filter(p -> !Main.getInstance().getVanishCommand().getVanishedPlayers().contains(p.getUniqueId()) || sender.hasPermission("tweaks.vanish"))
              .map(Player::getName)
              .filter(name -> name.toLowerCase().startsWith(input))
              .sorted(String.CASE_INSENSITIVE_ORDER)
              .collect(Collectors.toList());
    }

    if (args.length == 2) {
      List<String> times = List.of("30s", "10m", "1h", "1d");
      String input = args[1].toLowerCase();
      return times.stream()
              .filter(t -> t.startsWith(input))
              .collect(Collectors.toList());
    }

    return new ArrayList<>();
  }
}