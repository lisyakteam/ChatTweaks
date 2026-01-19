package me.junioraww.chattweaks.events;

import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GlobalHistoryEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  private final Component component;

  public GlobalHistoryEvent(Component component) {
    this.component = component;
  }

  public Component getComponent() {
    return component;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
