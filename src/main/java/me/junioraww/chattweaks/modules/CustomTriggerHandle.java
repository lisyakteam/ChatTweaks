package me.junioraww.chattweaks.modules;

import me.junioraww.chattweaks.Main;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class CustomTriggerHandle implements Listener {
  private final HashMap<UUID, PlayerData> stats = new HashMap<>();

  private final List<String> allowedFunctions = new ArrayList<>(List.of("cnk.cookbook_buttons", "cnk.distiller_book_buttons"));
  private final int MAX_COMMANDS = 6;
  private final long TIME_WINDOW = 2000;

  /*private final Main plugin;

  public CustomTriggerHandle(Main plugin) {
    this.plugin = plugin;
  }*/

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();

    String[] args = event.getMessage().split(" ");
    if (!player.isOp() && args.length >= 2) {
      if (Objects.equals(args[0], "/trigger") && allowedFunctions.stream().noneMatch(x -> args[1].equals(x))) {
        player.sendRichMessage("<red>Тебе нельзя такое, мальчик!");
        event.setCancelled(true);
        return;
      }
    }

    UUID uuid = player.getUniqueId();
    long now = System.currentTimeMillis();

    PlayerData data = stats.getOrDefault(uuid, new PlayerData(0, now));

    if (now - data.lastReset > TIME_WINDOW) {
      data.count = 1;
      data.lastReset = now;
    } else {
      data.count++;
    }

    stats.put(uuid, data);

    if (data.count >= MAX_COMMANDS) {
      event.setCancelled(true);
    }

    if (data.count == MAX_COMMANDS) {
      EquipmentSlot dropSlot = null;
      if (player.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK) dropSlot = EquipmentSlot.HAND;
      else if(player.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK) dropSlot = EquipmentSlot.OFF_HAND;
      
      if (dropSlot != null) {
        player.sendRichMessage("<gold>Книга выскользнула у вас из лап.\n<white>— Хмм, буду листать медленнее, - подумали вы.");
        player.playSound(Sound.sound(Key.key("entity.breeze.jump"), Sound.Source.AMBIENT, 1, 2));
        player.closeDialog();
        player.dropItem(dropSlot);
      }
      else player.sendRichMessage("<gold>Пожалуйста, помедленнее!");
    }
  }

  private static class PlayerData {
    int count;
    long lastReset;

    PlayerData(int count, long lastReset) {
      this.count = count;
      this.lastReset = lastReset;
    }
  }
}
