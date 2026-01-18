package me.junioraww.chattweaks.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientUtil {
  private static final Pattern GRADIENT_REGEX = Pattern.compile("gradient:(#?[A-Fa-f0-9]{6}):(#?[A-Fa-f0-9]{6})");

  public static Component apply(String name, String meta) {
    if (meta == null) return Component.text(name);

    Matcher m = GRADIENT_REGEX.matcher(meta);
    if (m.find()) {
      TextColor start = TextColor.fromHexString(m.group(1));
      TextColor end = TextColor.fromHexString(m.group(2));
      if (start == null || end == null) return Component.text(name);

      TextComponent.Builder builder = Component.text();
      for (int i = 0; i < name.length(); i++) {
        float ratio = (float) i / (float) Math.max(1, name.length() - 1);
        builder.append(Component.text(name.charAt(i), lerp(start, end, ratio)));
      }
      return builder.build();
    }

    TextColor solid = TextColor.fromHexString(meta.startsWith("#") ? meta : "#" + meta);
    return Component.text(name, solid != null ? solid : TextColor.color(255, 255, 255));
  }

  private static TextColor lerp(TextColor s, TextColor e, float r) {
    return TextColor.color(
            (int) (s.red() + (e.red() - s.red()) * r),
            (int) (s.green() + (e.green() - s.green()) * r),
            (int) (s.blue() + (e.blue() - s.blue()) * r)
    );
  }
}