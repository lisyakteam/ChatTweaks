package me.junioraww.chattweaks.modules;

import me.junioraww.chattweaks.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class Tab {

  private static final MiniMessage mm = MiniMessage.miniMessage();

  private static List<String> headerLines;
  private static List<String> footerLines;

  public static void init(YamlConfiguration settings) {
    headerLines = settings.getStringList("tab.header");
    footerLines = settings.getStringList("tab.footer");
  }

  public static void sendFancyTab(Player p) {

    String headerRaw = String.join("\n", headerLines)
            .replace("%player%", p.getName());

    String footerRaw = String.join("\n", footerLines)
            .replace("%online%", String.valueOf(Main.getInstance().getVanishCommand().online()))
            .replace("%ping%", String.valueOf(p.getPing()));

    Component header = mm.deserialize(headerRaw);
    Component footer = mm.deserialize(footerRaw);

    p.sendPlayerListHeaderAndFooter(header, footer);
  }
}