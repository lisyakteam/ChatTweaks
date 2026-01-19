package me.junioraww.chattweaks.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.regex.Pattern;

public class EmojiProcessor {
  private static Map<String, String> SPRITES = new HashMap<>();
  private static final Map<String, String> REVERSE_SPRITES = new HashMap<>();

  public static void load() {
    ArrayList<String> textures = new ArrayList<>(List.of(
            "pixel/confuse","pixel/purple-paw","pixel/cherry","pixel/pink-cookie","pixel/sleepy","pixel/cookie","pixel/magicwand","pixel/brownpaw","pixel/cloud","pixel/lily","pixel/blue-medusa","pixel/bigheart","pixel/cake","pixel/bluestars","pixel/flowers","pixel/ball","pixel/icecream","pixel/pony","pixel/waterlily","pixel/yellowstar","pixel/lilypad","pixel/coral","pixel/rainbow-flower","pixel/icecream-2","pixel/medusa","pixel/star","pixel/planet","pixel/happy-bone","pixel/yellow-heart","pixel/dog","pixel/starfish","pixel/bowtie-2","pixel/beauty","pixel/bowtie","pixel/blue-star","pixel/seaweed","pixel/paw","pixel/moonsky","pixel/hellokitty","pixel/croissant","pixel/toy","pixel/brown-icecream","pixel/yellow-icecream","pixel/white-icecream","pixel/berry","pixel/heartpaw","pixel/button","pixel/blue-heart","pixel/night","pixel/sad","pixel/dogfood","pixel/blue-planet","pixel/blue-cake","pixel/bone","pixel/butterfly","pixel/lily-2","pixel/glitter",
            "fox/facepalm","fox/wow","fox/assasin","fox/holiday","fox/flower","fox/salty","fox/fucked-2","fox/cool-3","fox/hentai","fox/burger-2","fox/police","fox/rain","fox/actually-2","fox/hehe","fox/sleep","fox/clenched","fox/smoorf","fox/bread","fox/pistol2","fox/money","fox/potato-2","fox/music","fox/hey-2","fox/angry","fox/clown","fox/halloween-dracula","fox/uno-reverse","fox/fire","fox/no","fox/time","fox/long-2","fox/shy","fox/gift","fox/crown","fox/dance","fox/backpack","fox/face-happy","fox/professor","fox/happy-3","fox/lick","fox/hello","fox/looking","fox/burger","fox/cry","fox/halloween-heart","fox/long-face","fox/money-static","fox/pirate","fox/bandit","fox/silly","fox/idk","fox/halloween-brain","fox/omfg","fox/offend-2","fox/cool","fox/crown2","fox/love","fox/actually","fox/shy-witch","fox/trash","fox/note","fox/evil","fox/snowman","fox/fucked","fox/zombie","fox/halloween-omfg","fox/bandit-2","fox/baby-2","fox/knight","fox/table","fox/need","fox/hmm-2","fox/hammer","fox/happy-fast","fox/yes","fox/aaa","fox/loupe","fox/pistol","fox/long-halloween-1","fox/potato","fox/dance-4","fox/toilet","fox/haha","fox/near","fox/pumpkin","fox/cringe","fox/fence","fox/baby","fox/pat","fox/long-3","fox/wtf","fox/love-2","fox/spray","fox/pride","fox/wtf-2","fox/tail","fox/smart","fox/dance-2","fox/umm","fox/uppies","fox/zombie-2","fox/long-hallowen-2","fox/paw","fox/jail","fox/hmm","fox/happy","fox/wreath","fox/bomb","fox/crown3","fox/t-shirt","fox/smart-2","fox/long-1","fox/hat","fox/bye","fox/warrior-2","fox/offend","fox/jam","fox/bandit-potato","fox/smile","fox/hey","fox/donut","fox/haha-2","fox/cool-2","fox/angry-2","fox/silly-2","fox/drink","fox/ate","fox/what","fox/scared","fox/angel","fox/king","fox/need2","fox/sad","fox/dance-3","fox/knife","fox/happy-2","fox/tent","fox/cool-4","fox/chicken","fox/full-face","fox/cry-2","fox/long-halloween-3","fox/ass","fox/timid","fox/photo","fox/halloween-ghost","fox/devil","fox/flower-2","fox/covid","fox/face","fox/popcorn","fox/warrior","fox/sleep-2","puro/omg","puro/amazed","puro/spin","puro/wiggle","puro/excited","puro/ping","puro/neutral","puro/pat","puro/happy"
    ));

    textures.forEach(texture -> {
      String[] parts = texture.split("/");
      String code;
      if (Objects.equals(parts[0], "pixel")) code = ':' + parts[1] + ':';
      else code = ':' + parts[0] + '-' + parts[1] + ':';
      SPRITES.put(code, texture);
      REVERSE_SPRITES.put("[emoji/" + texture + "@items]", code);
    });

    allEmojis = new LinkedList<>(SPRITES.keySet());
  }

  public static List<String> allEmojis;

  private static final Pattern COMBINED_PATTERN = Pattern.compile("(:[a-z0-9-]+:)|(\\[emoji/[a-z0-9/._-]+@items\\])");

  public static Component process(Component component) {
    return component.replaceText(TextReplacementConfig.builder()
        .match(COMBINED_PATTERN)
        .replacement((match, builder) -> {
          String matchedText = match.group();

          if (matchedText.startsWith(":")) {
            String sprite = SPRITES.get(matchedText);
            if (sprite != null) {
              return Component.object(ObjectContents.sprite(
                      Key.key("minecraft:items"),
                      Key.key("emoji/" + sprite)));
            }
          }

          else if (matchedText.startsWith("[")) {
            String code = REVERSE_SPRITES.get(matchedText);
            if (code != null) {
              String sprite = SPRITES.get(code);
              if (sprite != null) {
                return Component.object(ObjectContents.sprite(
                        Key.key("minecraft:items"),
                        Key.key("emoji/" + sprite)));
              }
            }
          }
          return builder;
        })
        .build());
  }

  public static Component deprocess(Component component) {
    if (component == null) return null;

    return convertObjectsToText(component);
  }

  private static Component convertObjectsToText(Component component) {
    Bukkit.getLogger().info("component " + component);
    if (component instanceof ObjectComponent oc) {
      if (oc.contents() instanceof SpriteObjectContents sprite) {
        Bukkit.getLogger().info(sprite.sprite().asMinimalString());
        String tag = "[" + sprite.sprite().asMinimalString() + "@items]";
        String code = REVERSE_SPRITES.get(tag);
        if (code != null) return Component.text(code).style(component.style());
      }
    }

    if (component.children().isEmpty()) return component;
    return component.children(component.children().stream()
            .map(EmojiProcessor::convertObjectsToText).toList());
  }
}