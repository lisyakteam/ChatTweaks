package me.junioraww.chattweaks.listeners;

import me.junioraww.chattweaks.Main;
import me.junioraww.chattweaks.events.GlobalChatEvent;
import me.junioraww.chattweaks.events.GlobalInfoEvent;
import me.junioraww.chattweaks.history.HistoryManager;
import me.junioraww.chattweaks.utils.*;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatEvents implements Listener {
  private final JavaPlugin plugin;
  private final HistoryManager history;
  private final Cooldown cooldown = new Cooldown(2, 30);
  private final PlayerAdapter<Player> lp;
  private final NamespacedKey mentionKey;
  private Advancement mentionAdv;

  public ChatEvents(JavaPlugin plugin) {
    this.plugin = plugin;
    this.history = new HistoryManager(plugin);
    this.lp = LuckPermsProvider.get().getPlayerAdapter(Player.class);
    this.mentionKey = new NamespacedKey(plugin, "mention_toast");
    setupAdvancement();
    Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> cooldown.cleanup(), 5, 5, TimeUnit.MINUTES);
  }

  public HistoryManager getHistory() {
    return history;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onChat(AsyncChatEvent event) {
    if (event.isCancelled()) return;
    event.setCancelled(true);

    final Player player = event.getPlayer();
    final String raw = PlainTextComponentSerializer.plainText().serialize(event.message());

    double cd = cooldown.check(player);
    if (cd > 0) {
      player.sendMessage(Component.text("Не так быстро!", NamedTextColor.YELLOW));
      return;
    }

    final boolean isGlobal = raw.startsWith("!");
    if (isGlobal && handleMute(player)) return;

    Bukkit.getLogger().info(player.getName() + ": " + raw);

    final String cleanMsg = isGlobal ? raw.substring(1).trim() : raw;

    Component msgContent = ChatFormatter.parseContent(cleanMsg, player.hasPermission("tweaks.colors"));
    msgContent = EmojiProcessor.process(msgContent);
    if (isGlobal) msgContent = msgContent.color(TextColor.color(0xfffce0));

    final Component name = ChatFormatter.formatName(player, lp.getMetaData(player));
    final Component sep = isGlobal
            ? Component.text(" ⟩⟩ ", TextColor.color(0xffbf00), TextDecoration.BOLD)
            : Component.text(" ⟩ ", TextColor.color(0x89CFF0), TextDecoration.BOLD);

    final Component baseComponent = Component.textOfChildren(name, sep, msgContent);

    List<Player> recipients = new ArrayList<>();
    if (isGlobal) {
      recipients.addAll(Bukkit.getOnlinePlayers());
    } else {
      World world = player.getWorld();
      Location playerLoc = player.getLocation();
      double distSq = 100.0 * 100.0;

      for (Player viewer : world.getPlayers()) {
        if (playerLoc.distanceSquared(viewer.getLocation()) <= distSq) {
          recipients.add(viewer);
        }
      }
    }

    int count = recipients.size();

    if (isGlobal) {
      Bukkit.getScheduler().runTask(plugin, task -> {
        new GlobalChatEvent(player.getName(), raw, count).callEvent();
      });
    }

    Component vipMsg = baseComponent.hoverEvent(ChatFormatter.getHover(player, count, true));
    Component regularMsg = baseComponent.hoverEvent(ChatFormatter.getHover(player, count, false));

    for (Player viewer : recipients) {
      viewer.sendMessage(viewer.hasPermission("tweaks.vip") ? vipMsg : regularMsg);
    }

    if (isGlobal) {
      history.add(baseComponent, baseComponent);
    }
    else {
      if (count <= 1) {
        player.sendMessage(Component.text("Ваше сообщение не увидели!\nЧтобы писать в глобальный чат, видный всем, добавьте ! перед сообщением", TextColor.color(0x89CFF0)));
      }
    }

    checkMentions(player, cleanMsg);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();;
    if (isVanished(player)) { event.joinMessage(null); return; }

    Component msg = joinMessage(player.getName());
    event.joinMessage(msg);

    var logs = history.get(player.hasPermission("tweaks.vip"));
    if (!logs.isEmpty()) player.sendMessage(Component.join(JoinConfiguration.newlines(), logs));

    /* Поддержка эмодзи-completion блять */
    player.addCustomChatCompletions(EmojiProcessor.allEmojis);

    history.add(msg, msg);
  }

  public static Component joinMessage(String name) {
    Bukkit.getScheduler().runTask(Main.getInstance(), task -> new GlobalInfoEvent(name + " зашел!").callEvent());
    return Component.text("+ ", NamedTextColor.GREEN).append(Component.text(name, NamedTextColor.GRAY));
  }

  public static Component quitMessage(String name) {
    Bukkit.getScheduler().runTask(Main.getInstance(), task -> new GlobalInfoEvent(name + " вышел!").callEvent());
    return Component.text("- ", NamedTextColor.RED).append(Component.text(name, NamedTextColor.GRAY));
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    if (isVanished(event.getPlayer())) { event.quitMessage(null); return; }

    Component msg = quitMessage(event.getPlayer().getName());
    event.quitMessage(msg);
    history.add(msg, msg);
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    if (isVanished(event.getEntity())) { event.deathMessage(null); return; }
    Component msg = Component.text().append(event.deathMessage()).color(TextColor.color(200, 200, 200)).build();
    event.deathMessage(msg);
    new GlobalInfoEvent(PlainTextComponentSerializer.plainText().serialize(event.deathMessage())).callEvent();
    history.add(msg, msg);
  }

  @EventHandler
  public void onAdv(PlayerAdvancementDoneEvent event) {
    if (event.getPlayer().isOp() || isVanished(event.getPlayer())) { event.message(null); return; }
    if (event.message() == null) return;
    Component msg = Component.text().append(event.message()).color(TextColor.color(200, 200, 200)).build();
    event.message(msg);
    history.add(msg, msg);
  }

  private boolean handleMute(Player p) {
    String path = "mutes." + p.getUniqueId();
    if (!Main.getSettings().contains(path)) return false;
    long end = Main.getSettings().getLong(path + ".end");
    if (end > System.currentTimeMillis()) {
      p.sendMessage(Component.text("Вы замучены!", NamedTextColor.RED));
      return true;
    }
    return false;
  }

  private void checkMentions(Player shooter, String msg) {
    for (Player target : Bukkit.getOnlinePlayers()) {
      if (msg.contains(target.getName())) {
        target.playSound(net.kyori.adventure.sound.Sound.sound(Sound.BLOCK_NOTE_BLOCK_BELL, net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f));
        // Логика тоста...
      }
    }
  }

  private boolean isVanished(Player p) {
    return Main.getInstance().getVanishCommand().getVanishedPlayers().contains(p.getUniqueId());
  }

  private void setupAdvancement() {
    mentionAdv = Bukkit.getAdvancement(mentionKey);
    if (mentionAdv != null) return;

    String json = "{"
            + "  \"display\": {"
            + "    \"icon\": {\"id\": \"minecraft:bell\"},"
            + "    \"title\": {\"text\": \"Вас упомянули!\", \"color\": \"yellow\"},"
            + "    \"description\": \"\","
            + "    \"frame\": \"goal\","
            + "    \"announce_to_chat\": false,"
            + "    \"show_toast\": true,"
            + "    \"hidden\": true"
            + "  },"
            + "  \"criteria\": {\"trigger\": {\"trigger\": \"minecraft:impossible\"}}"
            + "}";

    Bukkit.getUnsafe().loadAdvancement(mentionKey, json);
    mentionAdv = Bukkit.getAdvancement(mentionKey);
  }
}