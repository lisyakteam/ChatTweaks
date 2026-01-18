package me.junioraww.chattweaks.modules;

import me.junioraww.chattweaks.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutoMessages {
  private static MiniMessage serializer = MiniMessage.miniMessage();
  private static List<Component> messages;
  private static int next;

  public static void init(Main plugin) {
    messages = new LinkedList<>();

    var file = new File(plugin.getDataFolder(), "messages.yml");

    if (!plugin.getDataFolder().exists()) {
      plugin.getDataFolder().mkdirs();
    }

    if (!file.exists()) {
      plugin.saveResource("messages.yml", false);
    }

    var config = YamlConfiguration.loadConfiguration(file);
    config.getStringList("auto").forEach(message -> {
      messages.add(serializer.deserialize("<yellow><bold>☆</bold></yellow> " + message));
    });

    next = (int) (messages.size() * Math.random());

    Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
      send();
    }, 20, 20, TimeUnit.MINUTES);
  }

  private static void send() {
    Component message = messages.get(next);
    next++;
    if (next >= messages.size()) next = 0;
    Bukkit.getOnlinePlayers().forEach(player -> {
      player.sendMessage(message);
    });
  }
}
