package me.junioraww.chattweaks.listeners;

import me.junioraww.chattweaks.Main;
import me.junioraww.chattweaks.utils.EmojiProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

public class AnvilEvents implements Listener {

  @EventHandler
  public void onAnvilPrepare(PrepareAnvilEvent event) {
    AnvilInventory inventory = event.getInventory();
    AnvilView view = event.getView();

    ItemStack first = inventory.getFirstItem();
    ItemStack second = inventory.getSecondItem();

    if (first == null || first.getType() == Material.AIR) {
      return;
    }

    ItemStack result = event.getResult();

    boolean isCustomRecipe = false;
    int customCost = 0;

    if (second != null && second.getType() != Material.AIR) {
      if (result == null || result.getType() == Material.AIR) {
        result = first.clone();
      }

      ItemMeta meta = result.getItemMeta();
      if (meta == null) return;

      Component currentName;
      if (meta.hasDisplayName()) {
        currentName = EmojiProcessor.deprocess(meta.displayName());
      } else {
        currentName = Component.translatable(first.getType().translationKey());
      }

      String renameText = view.getRenameText();
      if (renameText != null && !renameText.trim().isEmpty()) {
        currentName = Component.text(renameText).style(currentName.style());
      }

      Material type = second.getType();
      boolean applied = false;

      if (type == Material.NETHER_STAR) {
        currentName = currentName.decoration(TextDecoration.BOLD, invert(currentName, TextDecoration.BOLD));
        customCost = 20;
        applied = true;
      } else if (type == Material.TORCHFLOWER) {
        TextDecoration.State currentState = currentName.decoration(TextDecoration.ITALIC);
        if (currentState == TextDecoration.State.NOT_SET || currentState == TextDecoration.State.TRUE) {
          currentName = currentName.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        } else {
          currentName = currentName.decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE);
        }
        customCost = 10;
        applied = true;
      } else if (type == Material.ECHO_SHARD) {
        currentName = currentName.decoration(TextDecoration.UNDERLINED, invert(currentName, TextDecoration.UNDERLINED));
        customCost = 15;
        applied = true;
      } else if (type.name().endsWith("_DYE") && second.getAmount() == 64) {
        try {
          DyeColor dye = DyeColor.valueOf(type.name().replace("_DYE", ""));
          currentName = currentName.color(TextColor.color(dye.getColor().asRGB()));
          customCost = 10;
          applied = true;
        } catch (IllegalArgumentException ignored) {}
      }

      if (applied) {
        meta.displayName(EmojiProcessor.process(currentName));
        result.setItemMeta(meta);
        event.setResult(result);
        isCustomRecipe = true;
      }
    }

    if (!isCustomRecipe && result != null && result.getType() != Material.AIR) {
      ItemMeta meta = result.getItemMeta();
      if (meta.hasDisplayName()) {
        meta.displayName(EmojiProcessor.process(meta.displayName()));
        result.setItemMeta(meta);
        event.setResult(result);
      }
    }

    if (isCustomRecipe) {
      int finalCost = view.getRepairCost() + customCost;
      if (finalCost >= 40 && !view.getPlayer().getGameMode().name().equals("CREATIVE")) {
        finalCost = 39;
      }
      view.setRepairCost(finalCost);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getInventory() instanceof AnvilInventory anvil)) return;

    if (event.getRawSlot() == 2) {
      if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

      ItemStack second = anvil.getSecondItem();
      if (second == null || second.getType() == Material.AIR) return;

      Material m = second.getType();
      boolean shouldConsume = false;

      if (m == Material.NETHER_STAR || m == Material.TORCHFLOWER || m == Material.ECHO_SHARD) {
        shouldConsume = true;
      } else if (m.name().endsWith("_DYE") && second.getAmount() == 64) {
        anvil.setSecondItem(new ItemStack(Material.AIR));
        return;
      }

      if (shouldConsume) {
        org.bukkit.Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
          ItemStack currentSecond = anvil.getSecondItem();
          if (currentSecond != null && currentSecond.getAmount() > 0) {
            currentSecond.setAmount(currentSecond.getAmount() - 1);
            anvil.setSecondItem(currentSecond);
          }
        });
      }
    }

    if (event.getRawSlot() == 0) {
      ItemStack item = event.getCurrentItem();
      if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
          meta.displayName(EmojiProcessor.process(meta.displayName()));
          item.setItemMeta(meta);
        }
      }
    }

    if (event.getRawSlot() == 0 || event.getRawSlot() == 1 || event.getCursor().getType() != Material.AIR) {
      org.bukkit.Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
        ItemStack item = anvil.getFirstItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
          meta.displayName(EmojiProcessor.deprocess(meta.displayName()));
          item.setItemMeta(meta);
        }
      });
    }
  }

  private TextDecoration.State invert(Component comp, TextDecoration deco) {
    return comp.hasDecoration(deco) ? TextDecoration.State.FALSE : TextDecoration.State.TRUE;
  }
}