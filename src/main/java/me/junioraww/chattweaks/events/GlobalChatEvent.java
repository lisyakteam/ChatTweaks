package me.junioraww.chattweaks.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GlobalChatEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  private final String username;
  private final String message;
  private final int recipients;

  public GlobalChatEvent(String username, String message, int recipients) {
    this.username = username;
    this.message = message;
    this.recipients = recipients;
  }

  public String getMessage() {
    return message;
  }

  public String getUsername() {
    return username;
  }

  public int getRecipients() {
    return recipients;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
