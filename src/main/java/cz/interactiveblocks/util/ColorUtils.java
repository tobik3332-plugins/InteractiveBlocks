package cz.interactiveblocks.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(builder, ChatColor.of("#" + color).toString());
        }
        matcher.appendTail(builder);

        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    public static Component parseComponent(String message) {
        if (message == null) return Component.empty();
        String translated = colorize(message);
        return MiniMessage.miniMessage().deserialize(translated);
    }
}
