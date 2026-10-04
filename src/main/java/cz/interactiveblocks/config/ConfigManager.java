package cz.interactiveblocks.config;

import cz.interactiveblocks.InteractiveBlocks;
import cz.interactiveblocks.util.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final InteractiveBlocks plugin;
    private FileConfiguration config;

    public ConfigManager(InteractiveBlocks plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getMessage(String path) {
        String msg = config.getString("messages." + path, "&cChybí text v config.yml: messages." + path);
        String prefix = config.getString("messages.prefix", "&8[&bInteractiveBlocks&8] ");
        return ColorUtils.colorize(prefix + msg);
    }

    public String getRawMessage(String path) {
        return ColorUtils.colorize(config.getString("messages." + path, ""));
    }
}
