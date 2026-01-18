package me.junioraww.chattweaks.history;

import me.junioraww.chattweaks.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HistoryManager {
  private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();
  private final File file;
  private final YamlConfiguration config = new YamlConfiguration();

  private final LinkedList<Component> defHistory = new LinkedList<>();
  private final LinkedList<Component> vipHistory = new LinkedList<>();

  public HistoryManager(JavaPlugin plugin) {
    this.file = new File(plugin.getDataFolder(), "history.yml");
    load();

    Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
      save();
      Main.getInstance().getLogger().info("Saved history.yml");
    }, 30L, 30L, TimeUnit.SECONDS);
  }

  private void load() {
    if (!file.exists()) return;
    try {
      config.load(file);
      config.getStringList("default").forEach(s -> defHistory.add(GSON.deserialize(s)));
      config.getStringList("vip").forEach(s -> vipHistory.add(GSON.deserialize(s)));
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void add(Component def, Component vip) {
    synchronized (this) {
      if (defHistory.size() >= 50) defHistory.removeFirst();
      defHistory.add(def);
      if (vipHistory.size() >= 60) vipHistory.removeFirst();
      vipHistory.add(vip);
    }
  }

  private void save() {
    List<String> dRaw = defHistory.stream().map(GSON::serialize).collect(Collectors.toList());
    List<String> vRaw = vipHistory.stream().map(GSON::serialize).collect(Collectors.toList());
    config.set("default", dRaw);
    config.set("vip", vRaw);
    try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
  }

  public List<Component> get(boolean isVip) {
    return isVip ? vipHistory : defHistory;
  }
}