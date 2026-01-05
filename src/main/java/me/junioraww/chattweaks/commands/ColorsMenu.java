package me.junioraww.chattweaks.commands;

import me.junioraww.chattweaks.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ColorsMenu implements CommandExecutor, Listener {

  private final LuckPerms luckPerms = LuckPermsProvider.get();
  private final String menuTitle = "Выберите цвет ника";
  private final String META_KEY = "chat-color";
  private final NamespacedKey COLOR_KEY;
  private final Main plugin;

  public ColorsMenu(Main plugin) {
    COLOR_KEY = new NamespacedKey(plugin, "nick_color_hex");
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) return true;

    Inventory gui = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(menuTitle));

    gui.addItem(createWool(Material.WHITE_WOOL, "Белый", "#F0F0F0"));
    gui.addItem(createWool(Material.ORANGE_WOOL, "Персиковый", "#FFB347"));
    gui.addItem(createWool(Material.MAGENTA_WOOL, "Лавандовый", "#C3B1E1"));
    gui.addItem(createWool(Material.LIGHT_BLUE_WOOL, "Небесный", "#89CFF0"));
    gui.addItem(createWool(Material.YELLOW_WOOL, "Лимонный", "#FDFD96"));
    gui.addItem(createWool(Material.LIME_WOOL, "Мятный", "#98FB98"));
    gui.addItem(createWool(Material.PINK_WOOL, "Розовый", "#FFB7CE"));
    gui.addItem(createWool(Material.RED_WOOL, "Коралловый", "#FF6961"));
    gui.addItem(createWool(Material.CYAN_WOOL, "Бирюзовый", "#A0E6FF"));
    gui.addItem(createWool(Material.PURPLE_WOOL, "Сиреневый", "#D6AEFF"));
    gui.addItem(createWool(Material.BLUE_WOOL, "Пастельно-синий", "#AEC6CF"));
    gui.addItem(createWool(Material.BROWN_WOOL, "Бежевый", "#F5F5DC"));
    gui.addItem(createWool(Material.GRAY_WOOL, "Стальной", "#B0C4DE"));
    gui.addItem(createWool(Material.LIGHT_GRAY_WOOL, "Серебристый", "#E5E4E2"));
    gui.addItem(createWool(Material.GREEN_WOOL, "Шалфей", "#B2AC88"));
    gui.addItem(createWool(Material.BLACK_WOOL, "Графитовый", "#555555"));

    ItemStack reset = new ItemStack(Material.BARRIER);
    ItemMeta resetMeta = reset.getItemMeta();
    resetMeta.displayName(MiniMessage.miniMessage().deserialize("<!italic><red>Сбросить цвет"));

    resetMeta.getPersistentDataContainer().set(COLOR_KEY, PersistentDataType.STRING, "RESET");
    reset.setItemMeta(resetMeta);
    gui.setItem(26, reset);

    player.openInventory(gui);
    return true;
  }

  public static void updatePlayerDisplay(Player player) {
    var luckPerms = LuckPermsProvider.get();
    var user = luckPerms.getUserManager().getUser(player.getUniqueId());
    if (user == null) return;

    var metaData = user.getCachedData().getMetaData();
    String prefix = Optional.ofNullable(metaData.getPrefix()).orElse("");
    String suffix = Optional.ofNullable(metaData.getSuffix()).orElse("");
    String colorMeta = metaData.getMetaValue("chat-color");

    String coloredName;
    if (colorMeta == null || colorMeta.isEmpty()) {
      coloredName = "<white>" + player.getName();
    } else if (colorMeta.startsWith("gradient:")) {
      coloredName = "<" + colorMeta + ">" + player.getName() + "</gradient>";
    } else {
      coloredName = "<color:" + colorMeta + ">" + player.getName() + "</color>";
    }

    var finalComponent = MiniMessage.miniMessage().deserialize(prefix + coloredName + suffix);

    player.playerListName(finalComponent);
    player.displayName(finalComponent);
  }

  private ItemStack createWool(Material material, String name, String hex) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();

    meta.displayName(MiniMessage.miniMessage().deserialize("<!italic><color:" + hex + ">" + name));

    meta.getPersistentDataContainer().set(COLOR_KEY, PersistentDataType.STRING, hex);

    item.setItemMeta(meta);
    return item;
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!event.getView().title().equals(MiniMessage.miniMessage().deserialize(menuTitle))) return;

    event.setCancelled(true);

    ItemStack clickedItem = event.getCurrentItem();
    if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getItemMeta() == null) return;

    if (event.getClickedInventory() != event.getView().getTopInventory()) return;

    String hex = clickedItem.getItemMeta().getPersistentDataContainer().get(COLOR_KEY, PersistentDataType.STRING);

    if (hex == null) return;

    Player player = (Player) event.getWhoClicked();
    updatePlayerColor(player, hex);
  }

  private void updatePlayerColor(Player player, String newHex) {
    User user = luckPerms.getUserManager().getUser(player.getUniqueId());
    if (user == null) return;

    if (newHex.equals("RESET")) {
      user.data().clear(node -> node instanceof MetaNode && ((MetaNode) node).getMetaKey().equals(META_KEY));
      player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Цвет ника сброшен."));
    } else {
      String currentColor = user.getCachedData().getMetaData().getMetaValue(META_KEY);
      String finalValue;

      if (currentColor == null || currentColor.contains("gradient")) {
        finalValue = newHex;
      } else {
        finalValue = "gradient:" + currentColor + ":" + newHex;
        player.closeInventory();
      }

      user.data().clear(node -> node instanceof MetaNode && ((MetaNode) node).getMetaKey().equals(META_KEY));
      user.data().add(MetaNode.builder(META_KEY, finalValue).build());
      player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Цвета обновлены!"));
    }

    luckPerms.getUserManager().saveUser(user).thenRun(() -> {
      Bukkit.getScheduler().runTask(plugin, () -> updatePlayerDisplay(player));
    });
  }
}