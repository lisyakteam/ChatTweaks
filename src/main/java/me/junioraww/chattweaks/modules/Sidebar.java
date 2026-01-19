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
  private static final List<BoardPage> pages = new ArrayList<>();
  private static int currentPageIndex = 0;
  private static String cachedOnline = "0";

  private static final Map<UUID, String[]> lastSentLines = new ConcurrentHashMap<>();

  public static void init(YamlConfiguration _settings) {
    settings = _settings;
    loadPages();

    Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
      if (pages.size() > 1) currentPageIndex = (currentPageIndex + 1) % pages.size();
    }, 0L, 30 * 20L);

    Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
      cachedOnline = String.valueOf(Main.getInstance().getVanishCommand().online());

      for (Player p : Bukkit.getOnlinePlayers()) {
        if (isHudDisabled(p)) continue;
        updateSidebarText(p);
      }
    }, 0L, 30L);
  }

  private static boolean isHudDisabled(Player p) {
    return Main.getSettings().getBoolean("player-settings." + p.getName().toLowerCase() + ".no-sidebar", false);
  }

  public static void setupPlayerScoreboard(Player p) {
    Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();
    Ping.setupForScoreboard(b);

    Objective obj = b.registerNewObjective("sidebar", Criteria.DUMMY, Component.empty());
    if (!isHudDisabled(p)) obj.setDisplaySlot(DisplaySlot.SIDEBAR);

    for (int i = 0; i < 15; i++) {
      String entry = String.valueOf(ChatColor.values()[i]);
      Team t = b.registerNewTeam("line" + i);
      t.addEntry(entry);
    }

    p.setScoreboard(b);
    lastSentLines.put(p.getUniqueId(), new String[15]);
  }

  private static void updateSidebarText(Player p) {
    if (pages.isEmpty()) return;
    Scoreboard b = p.getScoreboard();
    Objective obj = b.getObjective("sidebar");
    if (obj == null) return;

    BoardPage page = pages.get(currentPageIndex);
    obj.displayName(page.titleComp != null ? page.titleComp : mm.deserialize(page.rawTitle.replace("%player%", p.getName())));

    List<String> lines = page.lines;
    String[] cache = lastSentLines.get(p.getUniqueId());

    for (int i = 0; i < 15; i++) {
      String entry = String.valueOf(ChatColor.values()[i]);
      if (i < lines.size()) {
        String rawLine = lines.get(i)
                .replace("%online%", cachedOnline)
                .replace("%player%", p.getName())
                .replace("%ping%", String.valueOf(p.getPing()));

        if (cache != null && rawLine.equals(cache[i])) {
          continue;
        }

        if (cache != null) cache[i] = rawLine;
        b.getTeam("line" + i).prefix(mm.deserialize(rawLine));

        Score s = obj.getScore(entry);
        if (s.getScore() != lines.size() - i) s.setScore(lines.size() - i);
      } else {
        if (cache != null && cache[i] != null) {
          b.resetScores(entry);
          cache[i] = null;
        }
      }
    }
  }

  public static void clearPlayerCache(Player p) {
    lastSentLines.remove(p.getUniqueId());
    p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
  }

  public static void updateVisibility(Player p, boolean hidden) {
    Scoreboard b = p.getScoreboard();
    Objective obj = b.getObjective("sidebar");
    if (obj != null) {
      obj.setDisplaySlot(hidden ? null : DisplaySlot.SIDEBAR);
    }
    if (!hidden) updateSidebarText(p);
  }

  private static String getEntry(int i) {
    return ChatColor.values()[i].toString() + ChatColor.RESET;
  }

  public static void loadPages() {
    pages.clear();
    ConfigurationSection section = settings.getConfigurationSection("scoreboard.pages");
    if (section == null) return;
    for (String key : section.getKeys(false)) {
      String title = section.getString(key + ".title", "");
      pages.add(new BoardPage(title, title.contains("%") ? null : mm.deserialize(title), section.getStringList(key + ".lines")));
    }
  }

  private record BoardPage(String rawTitle, Component titleComp, List<String> lines) {}
}