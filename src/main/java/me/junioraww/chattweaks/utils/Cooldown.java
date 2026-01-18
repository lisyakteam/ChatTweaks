package me.junioraww.chattweaks.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cooldown {
  private final int maxAmount;
  private final int cooldownTicks;
  
  private final ConcurrentHashMap<UUID, CooldownData> timeouts = new ConcurrentHashMap<>();

  public Cooldown(int maxAmount, int ticks) {
    this.maxAmount = maxAmount;
    this.cooldownTicks = ticks;
  }

  private static class CooldownData {
    int count;
    int lastUpdateTick;

    CooldownData(int count, int lastUpdateTick) {
      this.count = count;
      this.lastUpdateTick = lastUpdateTick;
    }
  }

  public double check(Player player) {
    UUID uuid = player.getUniqueId();
    int currentTick = Bukkit.getCurrentTick();
    
    CooldownData data = timeouts.compute(uuid, (id, existingData) -> {
      if (existingData == null || (currentTick - existingData.lastUpdateTick) > cooldownTicks) {
        return new CooldownData(1, currentTick);
      }

      existingData.count++;
      return existingData;
    });

    if (data.count > maxAmount) {
      return cooldownTicks / 20.0;
    }

    return 0;
  }
  
  public void cleanup() {
    int currentTick = Bukkit.getCurrentTick();
    timeouts.entrySet().removeIf(entry -> (currentTick - entry.getValue().lastUpdateTick) > cooldownTicks);
  }
}