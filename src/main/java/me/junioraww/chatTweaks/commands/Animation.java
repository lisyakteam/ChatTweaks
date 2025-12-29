package me.junioraww.chatTweaks.commands;

import me.junioraww.chatTweaks.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Duration;

public class Animation implements CommandExecutor {
  private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) return true;

    int durationTicks = 60;

    // 1. Устанавливаем время появления и исчезновения (один раз)
    // fadeIn: 500ms, stay: 5000ms (будет обновляться), fadeOut: 500ms
    Title.Times times = Title.Times.times(
            Duration.ofMillis(0),
            Duration.ofMillis(200),
            Duration.ofMillis(0)
    );
    player.sendTitlePart(TitlePart.TIMES, times);

    final long startedAt = Bukkit.getCurrentTick();
    Bukkit.getScheduler().runTaskTimerAsynchronously(Main.get(), (task) -> {
      long tick = Bukkit.getCurrentTick() - startedAt;

      if (tick >= durationTicks) {
        task.cancel();
        return;
      }

      var titleText = serializer.deserialize(colorize("Coffee", true));
      var subtitleText = serializer.deserialize(colorize("С возвращением!", false));

      player.sendTitlePart(TitlePart.TITLE, titleText);
      player.sendTitlePart(TitlePart.SUBTITLE, subtitleText);

    }, 1L, 1L);

    return true;
  }

  public static String colorize(String text, boolean large) {
    String[] split = text.split("");
    StringBuilder colorized = new StringBuilder();
    float i = 0;
    float step = (float) (System.currentTimeMillis() % (text.length() * 350L) / 50);
    float frames = text.length() * 7;
    for(String symbol : split) {
      var RGB = getRGB(i + step, frames);
      var hex = getHex(RGB);
      colorized.append(hex);
      if (large) colorized.append("&l");
      colorized.append(symbol);
      i++;
    }
    return colorized.toString();
  }

  private static String getHex(int color) {
    String hex = Integer.toHexString(color).substring(2);
    return "&x" + hex.replaceAll("(.)","&$1");
  }

  private static int getRGB(float step, float frames) {
    int color = Color.HSBtoRGB(step / frames, 1.0F, 1.0F);
    return color;
  }
}
