package me.junioraww.chattweaks.listeners;

import me.junioraww.chattweaks.Main;
import me.junioraww.chattweaks.utils.EmojiProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
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

    if (first == null || first.getType() == Material.AIR) return;

    ItemStack result = event.getResult();
    if (result == null || result.getType() == Material.AIR) {
      result = first.clone();
    }

    ItemMeta resultMeta = result.getItemMeta();
    if (resultMeta == null) return;

    Component currentComponent;
    ItemMeta firstMeta = first.getItemMeta();

    if (firstMeta != null && firstMeta.hasDisplayName()) {
      currentComponent = EmojiProcessor.deprocess(firstMeta.displayName());
    } else {
      currentComponent = Component.translatable(first.getType().translationKey());
    }

    String renameText = view.getRenameText();
    if (renameText != null && !renameText.trim().isEmpty()) {
      currentComponent = Component.text(renameText).style(currentComponent.style());
    }

    int extraCost = 0;
    if (second != null && second.getType() != Material.AIR) {
      Material type = second.getType();

      if (type == Material.NETHER_STAR) {
        currentComponent = currentComponent.decoration(TextDecoration.BOLD, invert(currentComponent, TextDecoration.BOLD));
        extraCost = 21;
      }
      else if (type == Material.TORCHFLOWER) {
        TextDecoration.State currentItalic = currentComponent.decoration(TextDecoration.ITALIC);
        if (currentItalic == TextDecoration.State.FALSE) {
          currentComponent = currentComponent.decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE);
        } else {
          currentComponent = currentComponent.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }
        extraCost = 11;
      }
      else if (type == Material.ECHO_SHARD) {
        currentComponent = currentComponent.decoration(TextDecoration.UNDERLINED, invert(currentComponent, TextDecoration.UNDERLINED));
        extraCost = 11;
      }
      else if (type.name().endsWith("_DYE") && second.getAmount() == 64) {
        try {
          org.bukkit.DyeColor dye = org.bukkit.DyeColor.valueOf(type.name().replace("_DYE", ""));
          currentComponent = currentComponent.color(net.kyori.adventure.text.format.TextColor.color(dye.getColor().asRGB()));
          extraCost = 11;
        } catch (Exception ignored) {}
      }
    }

    Component finalName = EmojiProcessor.process(currentComponent);

    if (finalName.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
      finalName = finalName.decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE);
    }

    resultMeta.displayName(finalName);
    result.setItemMeta(resultMeta);
    event.setResult(result);

    if (extraCost > 0) {
      view.setRepairCost(view.getRepairCost() + extraCost);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getInventory() instanceof AnvilInventory anvil)) return;

    if (event.getRawSlot() == 0 || event.getCursor().getType() != Material.AIR) {
      org.bukkit.Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
        ItemStack item = anvil.getFirstItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta.hasCustomName()) {
          meta.customName(EmojiProcessor.deprocess(meta.customName()));
          item.setItemMeta(meta);
        }
      });
    }
  }

  private TextDecoration.State invert(Component comp, TextDecoration deco) {
    return comp.hasDecoration(deco) ? TextDecoration.State.FALSE : TextDecoration.State.TRUE;
  }
}