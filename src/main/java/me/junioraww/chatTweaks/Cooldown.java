package me.junioraww.chatTweaks;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class Cooldown {
  private final int seconds;

  public Cooldown(int seconds) {
    this.seconds = seconds;
  }

  private HashMap<String, Integer> cooldown = new HashMap<>();

  public int check(Player player) {
    int unixSec = (int) (System.currentTimeMillis() / 1000);
    String name = player.getName();
    if(cooldown.containsKey(name) && cooldown.get(name) > unixSec) return cooldown.get(name) - unixSec;
    cooldown.put(name, unixSec + seconds);
    return 0;
  }
}