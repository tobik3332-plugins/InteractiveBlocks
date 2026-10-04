package cz.interactiveblocks.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) return "";
        
        // Převod HEX kódů &#RRGGBB na §x§R§R§G§G§B§B
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hexCode.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        // Převod klasických & kódů
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static Component parseComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Pokud text obsahuje MiniMessage tagy (<green>, <bold> atd.), zpracujeme jej přes MiniMessage
        if (text.contains("<") && text.contains(">")) {
            try {
                // Převod případných legacy & kódů před předáním do MiniMessage, aby parser nespadl
                String legacyConverted = colorize(text);
                return LegacyComponentSerializer.legacySection().deserialize(legacyConverted);
            } catch (Exception ignored) {
                // V případě chybného tagu použijeme bezpečný fall-back přes legacy serializer
            }
        }

        // Jinak standardně převedeme & a § kódy na Component
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text.replace('§', '&'));
    }
}
