package me.junioraww.chattweaks.commands;

import me.junioraww.chattweaks.Main;
import me.junioraww.chattweaks.utils.ChatFormatter;
import me.junioraww.chattweaks.utils.GradientUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ColorsMenu implements CommandExecutor, Listener {

  private final Main plugin;
  private final NamespacedKey COLOR_KEY;
  private final String META_KEY = "chat-color";

  private final Map<UUID, String> pendingSelections = new ConcurrentHashMap<>();
  private final Map<UUID, List<Long>> clickHistory = new ConcurrentHashMap<>();

  public ColorsMenu(Main plugin) {
    this.plugin = plugin;
    this.COLOR_KEY = new NamespacedKey(plugin, "nick_color_hex");
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) return true;

    var user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
    String currentColor = (user != null) ? user.getCachedData().getMetaData().getMetaValue(META_KEY) : null;
    pendingSelections.put(player.getUniqueId(), currentColor != null ? currentColor : "#FFFFFF");

    openInventory(player);
    return true;
  }

  private void openInventory(Player player) {
    String currentSelection = pendingSelections.getOrDefault(player.getUniqueId(), "#FFFFFF");

    Component previewName = GradientUtil.apply(player.getName(), currentSelection);
    Component title = Component.text("Будет как: [")
            .append(previewName)
            .append(Component.text("]"));

    Inventory gui = Bukkit.createInventory(null, 27, title);

    addWool(gui, Material.WHITE_WOOL, "Белый", "#F0F0F0", currentSelection);
    addWool(gui, Material.ORANGE_WOOL, "Персиковый", "#FFB347", currentSelection);
    addWool(gui, Material.MAGENTA_WOOL, "Лавандовый", "#C3B1E1", currentSelection);
    addWool(gui, Material.LIGHT_BLUE_WOOL, "Небесный", "#89CFF0", currentSelection);
    addWool(gui, Material.YELLOW_WOOL, "Лимонный", "#FDFD96", currentSelection);
    addWool(gui, Material.LIME_WOOL, "Мятный", "#98FB98", currentSelection);
    addWool(gui, Material.PINK_WOOL, "Розовый", "#FFB7CE", currentSelection);
    addWool(gui, Material.RED_WOOL, "Коралловый", "#FF6961", currentSelection);
    addWool(gui, Material.CYAN_WOOL, "Бирюзовый", "#A0E6FF", currentSelection);
    addWool(gui, Material.PURPLE_WOOL, "Сиреневый", "#D6AEFF", currentSelection);
    addWool(gui, Material.BLUE_WOOL, "Пастельно-синий", "#AEC6CF", currentSelection);
    addWool(gui, Material.BROWN_WOOL, "Бежевый", "#F5F5DC", currentSelection);
    addWool(gui, Material.GRAY_WOOL, "Стальной", "#B0C4DE", currentSelection);
    addWool(gui, Material.LIGHT_GRAY_WOOL, "Серебристый", "#E5E4E2", currentSelection);
    addWool(gui, Material.GREEN_WOOL, "Шалфей", "#B2AC88", currentSelection);
    addWool(gui, Material.BLACK_WOOL, "Графитовый", "#555555", currentSelection);

    ItemStack reset = new ItemStack(Material.BARRIER);
    ItemMeta rMeta = reset.getItemMeta();
    rMeta.displayName(Component.text("Сбросить цвет", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
    rMeta.getPersistentDataContainer().set(COLOR_KEY, PersistentDataType.STRING, "RESET");
    reset.setItemMeta(rMeta);
    gui.setItem(18, reset);

    ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    ItemMeta cMeta = confirm.getItemMeta();
    cMeta.displayName(Component.text("Установить", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
    cMeta.getPersistentDataContainer().set(COLOR_KEY, PersistentDataType.STRING, "CONFIRM");
    confirm.setItemMeta(cMeta);
    gui.setItem(26, confirm);

    player.openInventory(gui);
  }

  private void addWool(Inventory inv, Material mat, String name, String hex, String currentSelection) {
    ItemStack item = new ItemStack(mat);
    ItemMeta meta = item.getItemMeta();

    meta.displayName(Component.text(name, TextColor.fromHexString(hex)).decoration(TextDecoration.ITALIC, false));
    meta.getPersistentDataContainer().set(COLOR_KEY, PersistentDataType.STRING, hex);

    if (currentSelection.contains(hex)) {
      meta.addEnchant(Enchantment.LURE, 1, true);
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    item.setItemMeta(meta);
    inv.addItem(item);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getView().title().toString().contains("Будет как:")) {
      event.setCancelled(true);
      Player player = (Player) event.getWhoClicked();
      ItemStack item = event.getCurrentItem();

      if (item == null || !item.hasItemMeta()) return;

      if (isSpamming(player)) {
        player.sendMessage(Component.text("Не так быстро!", NamedTextColor.RED));
        return;
      }

      String action = item.getItemMeta().getPersistentDataContainer().get(COLOR_KEY, PersistentDataType.STRING);
      if (action == null) return;

      UUID uuid = player.getUniqueId();

      if (action.equals("CONFIRM")) {
        applyFinalColor(player);
        return;
      }

      if (action.equals("RESET")) {
        pendingSelections.put(uuid, "#FFFFFF");
      } else {
        String current = pendingSelections.getOrDefault(uuid, "");
        if (current.isEmpty() || current.startsWith("gradient") || current.equals(action)) {
          pendingSelections.put(uuid, action);
        } else {
          pendingSelections.put(uuid, "gradient:" + current + ":" + action);
        }
      }

      openInventory(player);
    }
  }

  private boolean isSpamming(Player p) {
    long now = System.currentTimeMillis();
    List<Long> clicks = clickHistory.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>());

    clicks.removeIf(timestamp -> now - timestamp > 3000);

    if (clicks.size() >= 3) return true;

    clicks.add(now);
    return false;
  }

  private void applyFinalColor(Player player) {
    String finalColor = pendingSelections.get(player.getUniqueId());
    var lp = LuckPermsProvider.get();
    User user = lp.getUserManager().getUser(player.getUniqueId());

    if (user != null) {
      user.data().clear(node -> node instanceof MetaNode && ((MetaNode) node).getMetaKey().equals(META_KEY));

      if (finalColor != null && !finalColor.equals("#FFFFFF")) {
        user.data().add(MetaNode.builder(META_KEY, finalColor).build());
      }

      lp.getUserManager().saveUser(user).thenRun(() -> {
        Bukkit.getScheduler().runTask(plugin, () -> {
          updatePlayerDisplay(player);
          player.closeInventory();
          player.sendMessage(Component.text("Цвет ника успешно обновлен!", NamedTextColor.GREEN));
        });
      });
    }
  }

  public static void updatePlayerDisplay(Player player) {
    var lp = LuckPermsProvider.get();
    var user = lp.getUserManager().getUser(player.getUniqueId());
    if (user == null) return;

    var meta = user.getCachedData().getMetaData();
    Component nick = ChatFormatter.formatName(player, meta);

    player.displayName(nick);
    player.playerListName(nick);
  }
}