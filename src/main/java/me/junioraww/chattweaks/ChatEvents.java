package me.junioraww.chattweaks;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatEvents implements Listener {
  public final Cooldown cooldown;
  private static final MiniMessage serializer = MiniMessage.miniMessage();
  private final PlayerAdapter<Player> adapter;
  final boolean useGlobal = true;

  private final JavaPlugin plugin;

  private static final File chatHistoryFile = new File(Main.getInstance().getDataFolder(), "history.yml");
  private final YamlConfiguration savedChatHistory = new YamlConfiguration();

  private final LinkedList<Component> chatHistory = new LinkedList<>();
  private final LinkedList<Component> vipChatHistory = new LinkedList<>();

  public ChatEvents(JavaPlugin plugin) {
    this.plugin = plugin;
    this.cooldown = new Cooldown(1);
    var luckPerms = LuckPermsProvider.get();
    adapter = luckPerms.getPlayerAdapter(Player.class);

    loadChatHistory();
  }

  private void loadChatHistory() {
    if (!chatHistoryFile.exists()) {
      chatHistoryFile.getParentFile().mkdirs();
      try {
        chatHistoryFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    try {
      savedChatHistory.load(chatHistoryFile);
    } catch (Exception e) {
      e.printStackTrace();
    }

    List<String> rawDefault = savedChatHistory.getStringList("default");
    List<String> rawVip = savedChatHistory.getStringList("vip");

    chatHistory.clear();
    vipChatHistory.clear();

    rawDefault.forEach(s -> chatHistory.add(serializer.deserialize(s)));
    rawVip.forEach(s -> vipChatHistory.add(serializer.deserialize(s)));
  }

  private void saveChatHistory() {
    List<String> serializedDefault;
    List<String> serializedVip;

    synchronized (chatHistory) {
      serializedDefault = chatHistory.stream().map(serializer::serialize).collect(Collectors.toList());
    }
    synchronized (vipChatHistory) {
      serializedVip = vipChatHistory.stream().map(serializer::serialize).collect(Collectors.toList());
    }

    savedChatHistory.set("default", serializedDefault);
    savedChatHistory.set("vip", serializedVip);

    CompletableFuture.runAsync(() -> {
      try {
        savedChatHistory.save(chatHistoryFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void addChatHistory(Component component, Component vipComponent) {
    synchronized (chatHistory) {
      chatHistory.add(component);
      if (chatHistory.size() > 50) {
        chatHistory.removeFirst();
      }
    }
    synchronized (vipChatHistory) {
      vipChatHistory.add(vipComponent);
      if (vipChatHistory.size() > 60) {
        vipChatHistory.removeFirst();
      }
    }
    saveChatHistory();
  }

  @EventHandler(priority = EventPriority.LOW)
  public void playerJoined(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    var join = playerJoined(player.getName());
    event.joinMessage(join);
    List<Component> historyToSend;
    synchronized (chatHistory) {
      historyToSend = new LinkedList<>(player.hasPermission("tweaks.vip") ? vipChatHistory : chatHistory);
    }

    if(!historyToSend.isEmpty()) {
      Component merged = Component.join(JoinConfiguration.newlines(), historyToSend);
      player.sendMessage(merged);
    }

    addChatHistory(join, join);
  }

  public Component playerJoined(String name) {
    return serializer.deserialize("<green>+ <gray>" + name);
  }

  public Component playerLeft(String name) {
    return serializer.deserialize("<red>- <gray>" + name);
  }

  @EventHandler
  public void playerDied(PlayerDeathEvent event) {
    if (Main.getInstance().getVanishCommand().getVanishedPlayers().contains(event.getPlayer().getUniqueId())) {
      event.deathMessage(null);
      return;
    }
    var eventMessage = event.deathMessage();
    if (eventMessage == null) return;
    var message = Component.text().color(TextColor.color(200, 200, 200)).append(eventMessage).build();
    event.deathMessage(message);
    addChatHistory(message, message);
  }

  @EventHandler
  public void playerAdvancement(PlayerAdvancementDoneEvent event) {
    if (event.getPlayer().isOp() || Main.getInstance().getVanishCommand().getVanishedPlayers().contains(event.getPlayer().getUniqueId())) {
      event.message(null);
      return;
    }
    var eventMessage = event.message();
    if (eventMessage == null) return;
    var message = Component.text().color(TextColor.color(200, 200, 200)).append(eventMessage).build();
    event.message(message);
    addChatHistory(message, message);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void playerQuit(PlayerQuitEvent event) {
    if (Main.getInstance().getVanishCommand().getVanishedPlayers().contains(event.getPlayer().getUniqueId())) {
      event.quitMessage(null);
      return;
    }
    var quit = playerLeft(event.getPlayer().getName());
    event.quitMessage(quit);
    addChatHistory(quit, quit);
  }

  public

  Pattern urlPattern = Pattern.compile("(https?://\\S+)");

  @EventHandler(priority = EventPriority.LOW)
  public void chatEvent(AsyncChatEvent event) {
    if (event.isCancelled()) return;

    event.setCancelled(true);
    Component message = event.message();

    var player = event.getPlayer();
    var cd = cooldown.check(player);
    if (cd > 0) {
      player.sendMessage(serializer.deserialize("<yellow>Не так быстро!"));
      return;
    }

    var content = ((TextComponent) message).content();
    var viewers = event.viewers();

    CachedMetaData metaData = adapter.getMetaData(player);
    String prefix = Optional.ofNullable(metaData.getPrefix()).orElse("");
    String suffix = Optional.ofNullable(metaData.getSuffix()).orElse("");

    String colorMeta = metaData.getMetaValue("chat-color");
    String playerName = player.getName();
    String coloredName;

    if (colorMeta != null) {
      if (colorMeta.startsWith("gradient:")) {
        coloredName = "<" + colorMeta + ">" + playerName + "</gradient>";
      } else {
        coloredName = "<color:" + colorMeta + ">" + playerName + "</color>";
      }
    } else {
      coloredName = "<white>" + playerName + "</white>";
    }

    Component nameFormat = serializer.deserialize(prefix + coloredName + suffix);

    plugin.getLogger().info(prefix + player.getName() + suffix + ": " + content);

    if (useGlobal) {
      var chatType = content.charAt(0) == '!' ? ChatTypes.GLOBAL : ChatTypes.LOCAL;
      Location location = player.getLocation();
      Vector origin = new Vector(location.getX(), 0, location.getBlockZ());

      if (chatType == ChatTypes.GLOBAL) {
        content = content.substring(1).stripLeading();
      }

      Matcher matcher = urlPattern.matcher(content);

      int lastEnd = 0;
      Component parsedContent = Component.empty();

      while (matcher.find()) {
        String before = content.substring(lastEnd, matcher.start());
        if (!before.isEmpty()) parsedContent = parsedContent.append(Component.text(before));

        String url = matcher.group();
        Component link = Component.text("Ссылка")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url));

        parsedContent = parsedContent.append(link);
        lastEnd = matcher.end();
      }

      if (lastEnd < content.length()) {
        parsedContent = parsedContent.append(Component.text(content.substring(lastEnd)));
      }

      Component visible = Component.textOfChildren(chatType.prefix, nameFormat, Component.text(": "), parsedContent).hoverEvent(getDefaultAbout(player));

      List<Player> receivers = new LinkedList<>();
      int counter = 0;

      for (Audience viewer : viewers) {
        String receiverName = viewer.getOrDefault(Identity.NAME, null);
        if (receiverName != null) {
          Player receiver = Bukkit.getPlayerExact(receiverName);
          if (receiver != null) {
            var receiverLoc = receiver.getLocation();
            Vector point = new Vector(receiverLoc.getX(), 0, receiverLoc.getBlockZ());
            if (chatType == ChatTypes.LOCAL) {
              if (origin.distance(point) > 100) continue;
            }
            if (receiver.getGameMode() != GameMode.SPECTATOR) counter++;
            receivers.add(receiver);
          }
        }
      }

      Component vip = visible.hoverEvent(getVIPAbout(player, counter));

      for (Player receiver : receivers) {
        if (receiver.hasPermission("tweaks.vip")) receiver.sendMessage(vip);
        else receiver.sendMessage(visible);
      }

      if (chatType == ChatTypes.GLOBAL) {
        addChatHistory(visible, vip);
      }

      if (counter == 1 && chatType == ChatTypes.LOCAL) {
        player.sendRichMessage("<yellow>Ваше сообщение никто не увидел!"
                + "\n<white>Чтобы писать в <gold>глобальный чат</gold> (видный всем), пишите восклицательный знак в начале сообщения.</white>");
      }
    } else {
      Component parsedContent = player.hasPermission("tweaks.colors") ? serializer.deserialize(content) : Component.text(content);
      Component visible = Component.textOfChildren(nameFormat, defaultChatSeparator, parsedContent);

      for (Audience viewer : viewers) {
        String receiverName = viewer.getOrDefault(Identity.NAME, null);
        if (receiverName != null) {
          Player receiver = Bukkit.getPlayerExact(receiverName);
          if (receiver != null) receiver.sendMessage(visible);
        } else viewer.sendMessage(serializer.deserialize(player.getName() + ": " + content));
      }
    }
  }

  private HoverEvent<Component> getVIPAbout(Player player, int receivers) {
    Component text = serializer.deserialize(
            "<gold><bold>↯</bold> <#ffcc00>Время: " + dtf.format(LocalTime.now())
                    + "\n<gradient:#42ff9e:white>Увидело " + receivers + " из " + Main.getInstance().getVanishCommand().online()
                    + "\n<gray>Мир: <white>" + player.getWorld().getName()
                    + "</white>\n<gray>Пинг игрока: <white>" + player.getPing()
                    + "</white>\n<gray>Прорисовка: <white>" + player.getClientViewDistance()
                    + "</white>"
    );
    return HoverEvent.showText(text);
  }

  public static String getWorldName(World world) {
    var environment = world.getEnvironment();
    if (environment == World.Environment.NORMAL) return "<blue>Верхний";
    if (environment == World.Environment.NETHER) return "<red>Нижний";
    if (environment == World.Environment.THE_END) return "<purple>Энд";
    return "<yellow>Новый";
  }

  private HoverEvent<Component> getDefaultAbout(Player player) {
    Component text = serializer.deserialize(
            "<gold><bold>↯</bold> <#ffcc00>Время: " + dtf.format(LocalTime.now())
                    + "\n<gray>Мир: <white>" + getWorldName(player.getWorld())
                    + "</white>\n<gray>Пинг игрока: <white><bold>" + player.getPing()
                    + "</bold></white>\n<gray>Прорисовка: <white><bold>" + player.getClientViewDistance()
                    + "</bold></white>"
    );
    return HoverEvent.showText(text);
  }

  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
  private final Component defaultChatSeparator = serializer.deserialize("<white>: ");

  public enum ChatTypes {
    LOCAL("<gray><bold>[L]</bold></gray> "),
    GLOBAL("<gold><bold>[G]</bold></gold> ");

    final Component prefix;

    ChatTypes(String prefix) {
      this.prefix = serializer.deserialize(prefix);
    }
  }
}