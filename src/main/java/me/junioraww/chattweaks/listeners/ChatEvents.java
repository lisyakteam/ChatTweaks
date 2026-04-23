package me.junioraww.chattweaks.listeners;

import com.destroystokyo.paper.profile.ProfileProperty;
import me.junioraww.chattweaks.Main;
import me.junioraww.chattweaks.events.GlobalHistoryEvent;
import me.junioraww.chattweaks.events.GlobalInfoEvent;
import me.junioraww.chattweaks.history.HistoryManager;
import me.junioraww.chattweaks.utils.*;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChatEvents implements Listener {
  private final Main plugin;
  private final HistoryManager history;
  private final Cooldown cooldown = new Cooldown(2, 30);
  private final PlayerAdapter<Player> lp;
  private final NamespacedKey mentionKey;
  private Advancement mentionAdv;

  public ChatEvents(Main plugin) {
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

  static double distSq = 100.0 * 100.0;

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
    if (isGlobal) msgContent = msgContent.color(TextColor.color(0xffcf40));
    msgContent = EmojiProcessor.process(msgContent);

    Component playerHead = Component.object(
            ObjectContents.playerHead().profileProperty(
                    new ChatHead(player)
            ).build()
    );

    final Component name = me.junioraww.tails.listeners.Nametag.getDisplayedName(player);
    final Component sep = isGlobal
            ? Component.text(" ⟩⟩ ", TextColor.color(0xffbf00), TextDecoration.BOLD)
            : Component.text(" ⟩ ", TextColor.color(0x89CFF0), TextDecoration.BOLD);

    final Component baseComponent = Component.textOfChildren(playerHead, Component.space(), name, sep, msgContent);

    Bukkit.getScheduler().runTask(plugin, () -> {
      if (isGlobal) new GlobalInfoEvent(player.getName() + ": " + cleanMsg).callEvent();
      handleChatSync(player, isGlobal, baseComponent, cleanMsg);
    });
  }

  private class ChatHead implements PlayerHeadObjectContents.ProfileProperty {
    private Player player;

    public ChatHead(Player player) {
      this.player = player;
    }

    @Override
    public @NotNull String name() {
      return "textures";
    }

    @Override
    public @NotNull String value() {
      return player.getPlayerProfile()
              .getProperties()
              .stream()
              .filter(p -> p.getName().equals("textures"))
              .findFirst()
              .map(ProfileProperty::getValue).get();
    }

    @Override
    public @Nullable String signature() {
      return "";
    }
  }

  private void handleChatSync(Player player, boolean isGlobal, Component baseComponent, String cleanMsg) {
    int count = 0;

    List<Player> recipients = new ArrayList<>();
    if (isGlobal) {
      recipients.addAll(Bukkit.getOnlinePlayers());
    } else {
      World world = player.getWorld();
      Location playerLoc = player.getLocation();

      for (Player viewer : world.getPlayers()) {
        if (playerLoc.distanceSquared(viewer.getLocation()) <= distSq) {
          recipients.add(viewer);
          if (!viewer.getGameMode().equals(GameMode.SPECTATOR) && !isVanished(viewer)) count++;
        }
      }
    }

    Component vipMsg = baseComponent.hoverEvent(ChatFormatter.getHover(player, count, true));
    Component regularMsg = baseComponent.hoverEvent(ChatFormatter.getHover(player, count, false));

    for (Player viewer : recipients) {
      viewer.sendMessage(viewer.hasPermission("tweaks.vip") ? vipMsg : regularMsg);
    }

    if (isGlobal) {
      history.add(regularMsg, vipMsg);
    }
    else {
      if (count <= 1) {
        player.sendMessage(Component.text("Ваше сообщение не увидели!\nЧтобы писать в глобальный чат, видный всем, добавьте ! перед сообщением", TextColor.color(0x89CFF0)));
      }
    }

    checkMentions(player, cleanMsg, recipients);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();;
    if (isVanished(player)) { event.joinMessage(null); return; }

    //Component msg = joinMessage(player.getName());
    event.joinMessage(null);

    var logs = history.get(player.hasPermission("tweaks.vip"));
    if (!logs.isEmpty()) player.sendMessage(Component.join(JoinConfiguration.newlines(), logs));

    /* Поддержка эмодзи-completion блять */
    player.addCustomChatCompletions(EmojiProcessor.allEmojis);

    //history.add(msg, msg);
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

    //Component msg = quitMessage(event.getPlayer().getName());
    event.quitMessage(null);
    //history.add(msg, msg);
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
  public void onTelegramEvent(GlobalHistoryEvent event) {
    var component = event.getComponent();
    history.add(component, component);
  }

  @EventHandler
  public void onAdv(PlayerAdvancementDoneEvent event) {
    event.message(null);
    if (isVanished(event.getPlayer())) { return; }

    var display = event.getAdvancement().getDisplay();
    if (display == null || !display.doesAnnounceToChat()) {
      event.message(null);
      return;
    }

    String frameKey = "chat.type.advancement." + display.frame().name().toLowerCase();

    Component message = Component.translatable(frameKey)
            .args(
                    event.getPlayer().name(),
                    display.title().hoverEvent(display.description())
            )
            .color(TextColor.color(200, 200, 200));

    for (var player : Bukkit.getOnlinePlayers()) {
      player.sendMessage(message);
    }

    Component translated = GlobalTranslator.render(message, Locale.forLanguageTag("ru-RU"));
    String plainText = PlainTextComponentSerializer.plainText().serialize(translated);
    new GlobalInfoEvent(plainText).callEvent();

    history.add(message, message);
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

  private void checkMentions(Player shooter, String msg, List<Player> targets) {
    for (Player target : targets) {
      if (msg.contains(target.getName())) {
        target.playSound(net.kyori.adventure.sound.Sound.sound(Sound.BLOCK_NOTE_BLOCK_BELL, net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f));

        Bukkit.getScheduler().runTask(plugin, task -> {
          target.getAdvancementProgress(mentionAdv).revokeCriteria("trigger");
          target.getAdvancementProgress(mentionAdv).awardCriteria("trigger");
        });
      }
    }
  }

  private boolean isVanished(Player p) {
    return Main.getInstance().getVanishCommand().getVanishedPlayers().contains(p.getUniqueId());
  }

  private void setupAdvancement() {
    mentionAdv = Bukkit.getAdvancement(mentionKey);
    if (mentionAdv != null) return;

    Bukkit.getScheduler().runTask(plugin, task -> {
      String json = "{"
              + "  \"display\": {"
              + "    \"icon\": {\"id\": \"minecraft:bell\"},"
              + "    \"background\": \"minecraft:block/mushroom_stem\","
              + "    \"title\": {\"text\": \"Вас упомянули!\", \"color\": \"yellow\"},"
              + "    \"description\": \"\","
              + "    \"frame\": \"task\","
              + "    \"announce_to_chat\": false,"
              + "    \"show_toast\": true,"
              + "    \"hidden\": true"
              + "  },"
              + "  \"criteria\": {\"trigger\": {\"trigger\": \"minecraft:impossible\"}}"
              + "}";

      Bukkit.getUnsafe().loadAdvancement(mentionKey, json);
      mentionAdv = Bukkit.getAdvancement(mentionKey);
    });
  }
}