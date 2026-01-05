package me.junioraww.chattweaks.modules;

import me.junioraww.chattweaks.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Sidebar {
  private static final MiniMessage mm = MiniMessage.miniMessage();
  private static YamlConfiguration settings;

  private static final Map<UUID, Scoreboard> boards = new ConcurrentHashMap<>();
  private static final Set<UUID> hiddenPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

  private static final List<BoardPage> pages = new ArrayList<>();
  private static int currentPageIndex = 0;
  private static String cachedOnline = "0";

  public static void init(YamlConfiguration _settings) {
    settings = _settings;
    loadPages();

    Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
      if (pages.size() > 1) {
        currentPageIndex = (currentPageIndex + 1) % pages.size();
      }
    }, 0L, 30 * 20L);

    Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
      cachedOnline = Main.getInstance().getVanishCommand().online() + "";

      for (Player p : Bukkit.getOnlinePlayers()) {
        if (hiddenPlayers.contains(p.getUniqueId())) continue;
        updateSidebarText(p);
      }
    }, 0L, 20L);
  }

  public static void togglePlayerHud(Player p, boolean hidden) {
    if (hidden) {
      hiddenPlayers.add(p.getUniqueId());
      p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      boards.remove(p.getUniqueId());
    } else {
      hiddenPlayers.remove(p.getUniqueId());
      setupPlayerScoreboard(p);
    }
  }

  public static void loadPages() {
    pages.clear();
    ConfigurationSection section = settings.getConfigurationSection("scoreboard.pages");
    if (section == null) return;

    for (String key : section.getKeys(false)) {
      String title = section.getString(key + ".title", "");
      List<String> lines = section.getStringList(key + ".lines");

      Component titleComp = title.contains("%") ? null : mm.deserialize(title);
      pages.add(new BoardPage(title, titleComp, lines));
    }
  }

  public static void setupPlayerScoreboard(Player p) {
    UUID uuid = p.getUniqueId();
    Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();

    Objective obj = b.registerNewObjective("sidebar", Criteria.DUMMY, Component.empty());
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);

    Ping.setupForScoreboard(b);

    for (int i = 0; i < 15; i++) {
      String entry = getEntry(i);
      Team t = b.registerNewTeam("line" + i);
      t.addEntry(entry);
    }

    boards.put(uuid, b);
    p.setScoreboard(b);
    updateSidebarText(p); // Сразу обновляем текст
  }

  private static void updateSidebarText(Player p) {
    Scoreboard b = boards.get(p.getUniqueId());
    if (b == null || pages.isEmpty()) return;

    BoardPage page = pages.get(currentPageIndex);
    Objective obj = b.getObjective("sidebar");
    if (obj == null) return;

    if (page.titleComp != null) {
      obj.displayName(page.titleComp);
    } else {
      obj.displayName(mm.deserialize(page.rawTitle.replace("%player%", p.getName())));
    }

    List<String> lines = page.lines;
    for (int i = 0; i < 15; i++) {
      String entry = getEntry(i);
      if (i < lines.size()) {
        String rawLine = lines.get(i);

        String formatted = rawLine
                .replace("%online%", cachedOnline)
                .replace("%player%", p.getName())
                .replace("%ping%", String.valueOf(p.getPing()));

        b.getTeam("line" + i).prefix(mm.deserialize(formatted));

        Score s = obj.getScore(entry);
        int targetScore = lines.size() - i;
        if (s.getScore() != targetScore) s.setScore(targetScore);
      } else {
        b.resetScores(entry);
      }
    }
  }

  private static String getEntry(int i) {
    return ChatColor.values()[i].toString() + ChatColor.RESET;
  }

  public static void clearPlayerCache(Player p) {
    UUID uuid = p.getUniqueId();
    boards.remove(uuid);
    hiddenPlayers.remove(uuid);
  }

  private record BoardPage(String rawTitle, Component titleComp, List<String> lines) {}
}