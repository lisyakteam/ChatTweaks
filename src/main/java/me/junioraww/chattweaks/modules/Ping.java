package me.junioraww.chattweaks.modules;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.*;

public class Ping implements Listener {

  public static void setupForScoreboard(Scoreboard board) {
    Objective obj = board.getObjective("ping");
    if (obj == null) {
      obj = board.registerNewObjective("ping", Criteria.DUMMY, Component.text("ms"));
      obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
      obj.setRenderType(RenderType.INTEGER);
    }
  }

  public static void updatePings() {
    for (Player target : Bukkit.getOnlinePlayers()) {
      int currentPing = target.getPing();

      for (Player viewer : Bukkit.getOnlinePlayers()) {
        Scoreboard board = viewer.getScoreboard();
        Objective obj = board.getObjective("ping");

        if (obj != null) {
          obj.getScore(target.getName()).setScore(currentPing);
        }
      }
    }
  }
}