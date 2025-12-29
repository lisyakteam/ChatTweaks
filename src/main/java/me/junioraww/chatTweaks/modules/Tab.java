package me.junioraww.chatTweaks.modules;

import me.junioraww.chatTweaks.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class Tab {
    private static MiniMessage serializer = MiniMessage.miniMessage();
    private static YamlConfiguration settings;

    public static void init(YamlConfiguration _settings) {
        settings = _settings;
    }

    public static void sendFancyTab(Player p) {
        Component header = Component.text("╔═══ ", NamedTextColor.GOLD)
                .append(Component.text("Добро пожаловать ", NamedTextColor.YELLOW))
                .append(Component.text("в клуб!", NamedTextColor.AQUA))
                .append(Component.text(" ═══╗", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.text("Игрок: ", NamedTextColor.GRAY))
                .append(Component.text(p.getName(), NamedTextColor.GREEN))
                .append(Component.newline());

        Component footer = Component.newline()
                .append(Component.text("Онлайн: ", NamedTextColor.GRAY))
                .append(Component.text(Bukkit.getOnlinePlayers().size(), NamedTextColor.GREEN))
                .append(Component.text("  -  Приятной игры!", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.text("══════════════════════", NamedTextColor.AQUA));

        p.sendPlayerListHeaderAndFooter(header, footer);
    }

  public static void setSidebar(Player p) {
    if (!settings.getBoolean(p.getName().toLowerCase() + ".no-sidebar")) return;

    /*
    var scoreboard = Main.getScoreboard();
    Objective obj;
    if (scoreboard.getObjective(DisplaySlot.SIDEBAR) == null) {
      obj = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, Component.empty());
      obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    } else obj = scoreboard.getObjective(DisplaySlot.SIDEBAR);

    obj.getScore("§aПриятной игры!").setScore(7);
    obj.getScore("§7Онлайн: §f" + Bukkit.getOnlinePlayers().size()).setScore(6);
    obj.getScore("§r").setScore(5);
    obj.getScore("§8Скрыть: /h").setScore(4);
    obj.getScore("§8Паки: /pack").setScore(3);
    obj.getScore("§r ").setScore(2);

    String entryKey = "§0§r";

    Team team = Main.getScoreboard().getTeam("ip_line");
    if (team == null) team = Main.getScoreboard().registerNewTeam("ip_line");

    team.addEntry(entryKey);

    Component gradient = serializer.deserialize("<gradient:#AAC4F5:#F9DFDF><bold>IP: лисяк.рф</bold></gradient>");

    try {
      team.prefix(gradient);
    } catch (Exception e) {
      Bukkit.getLogger().warning("Слишком длинный префикс для скорборда!");
      team.prefix(Component.text("IP лисяк.рф", NamedTextColor.GREEN));
    }

    obj.getScore(entryKey).setScore(1);*/
  }




}
