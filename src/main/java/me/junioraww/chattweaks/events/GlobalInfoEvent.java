package me.junioraww.chattweaks.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GlobalInfoEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  private final String text;

  public GlobalInfoEvent(String text) {
    this.text = text;
  }
  
  public String getUsername() {
    return text;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
