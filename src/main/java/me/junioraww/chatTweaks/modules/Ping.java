package me.junioraww.chatTweaks.modules;

import me.junioraww.chatTweaks.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Ping implements Listener {
  private static Objective pingObjective;

  public static void setupScoreboard() {
    pingObjective = Main.getScoreboard().registerNewObjective("ping", Criteria.DUMMY, Component.text("ms"));
    pingObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    pingObjective.setRenderType(RenderType.INTEGER);
  }

  @EventHandler
  public void onLeft(PlayerQuitEvent event) {
    pingObjective.getScore(event.getPlayer().getName()).resetScore();
  }

  public static void updatePings() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      int currentPing = player.getPing();
      pingObjective.getScore(player.getName()).setScore(currentPing);
    }
  }
}
