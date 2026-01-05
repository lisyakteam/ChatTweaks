package me.junioraww.chattweaks.modules;

import me.junioraww.chattweaks.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Tab {
  private static final MiniMessage mm = MiniMessage.miniMessage();

  public static void sendFancyTab(Player p) {
    Component header = mm.deserialize(
            "<gray>--- <gradient:#AAC4F5:#F9DFDF><b>ЛИСЯК.РФ</b></gradient> ---</gray>\n" +
                    "<gray>Добро пожаловать, <white>" + p.getName() + "</white>!\n"
    );

    Component footer = mm.deserialize(
            "\n<gray>Онлайн: <white>" + Main.getInstance().getVanishCommand().online() + "</white> " +
                    "  <gray>Пинг: <white>" + p.getPing() + "ms</white>\n" +
                    "\n" +
                    "<gradient:#AAC4F5:#F9DFDF>t.me/LisyakTeam</gradient>\n" +
                    "<gradient:#F9DFDF:#AAC4F5>discord.gg/MC6ccff</gradient>\n" +
                    "\n" +
                    "<dark_gray>Приятной игры на нашем проекте!"
    );

    p.sendPlayerListHeaderAndFooter(header, footer);
  }

  public static void init(org.bukkit.configuration.file.YamlConfiguration settings) {

  }
}