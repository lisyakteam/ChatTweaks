package me.junioraww.chattweaks.utils;

import me.junioraww.chattweaks.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ChatFormatter {
  private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+)");

  public static Component formatName(Player p, CachedMetaData meta) {
    Component head = Component.object(ObjectContents.playerHead(p.getName()));
    Component name = GradientUtil.apply(p.getName(), meta.getMetaValue("chat-color"));
    Component prefix = Component.text(meta.getPrefix() != null ? meta.getPrefix() : "");
    Component suffix = Component.text(meta.getSuffix() != null ? meta.getSuffix() : "");

    return Component.textOfChildren(head, Component.space(), prefix, name, suffix);
  }

  public static Component parseContent(String msg, boolean hasColor) {
    Component base = Component.text(msg);

    return base.replaceText(t -> t.match(URL_PATTERN).replacement(url ->
            Component.text("Ссылка", NamedTextColor.AQUA)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.openUrl(url.content()))
    ));
  }

  public static HoverEvent<Component> getHover(Player p, int viewers, boolean isVip) {
    TextComponent.Builder b = Component.text()
            .append(Component.text("↯ ", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("Время: " + DTF.format(LocalTime.now()), TextColor.color(0xffcc00)))
            .append(Component.newline())
            .append(Component.text("IP: ", TextColor.color(0xffcc00)))
            .append(Component.text((int) (Math.random() * 255) + "."+ (int) (Math.random() * 255) +
                    "."+ (int) (Math.random() * 255) + "."+ (int) (Math.random() * 255), TextColor.color(0xffffff)))
            .append(Component.newline());

    if (isVip) {
      b.append(Component.text("Увидело " + viewers + " из " + Main.getInstance().getVanishCommand().online(), TextColor.color(0x42ff9e)))
              .append(Component.newline());
    }

    b.append(Component.text("Мир: ", NamedTextColor.GRAY)).append(getWorldComp(p.getWorld())).append(Component.newline())
            .append(Component.text("Пинг: ", NamedTextColor.GRAY)).append(Component.text(p.getPing(), NamedTextColor.WHITE)).append(Component.newline())
            .append(Component.text("Прорисовка: ", NamedTextColor.GRAY)).append(Component.text(p.getClientViewDistance(), NamedTextColor.WHITE));

    return HoverEvent.showText(b.build());
  }

  private static Component getWorldComp(World w) {
    return switch (w.getEnvironment()) {
      case NORMAL -> Component.text("Верхний", NamedTextColor.BLUE);
      case NETHER -> Component.text("Нижний", NamedTextColor.RED);
      case THE_END -> Component.text("Энд", NamedTextColor.LIGHT_PURPLE);
      default -> Component.text("Новый", NamedTextColor.YELLOW);
    };
  }
}