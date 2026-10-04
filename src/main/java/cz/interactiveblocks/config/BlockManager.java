package cz.interactiveblocks.config;

import cz.interactiveblocks.InteractiveBlocks;
import cz.interactiveblocks.model.InteractionType;
import cz.interactiveblocks.model.InteractiveBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BlockManager {

    private final InteractiveBlocks plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, InteractiveBlock> blocks = new HashMap<>();

    public BlockManager(InteractiveBlocks plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "iblocks.yml");
        loadBlocks();
    }

    public void loadBlocks() {
        if (!file.exists()) {
            plugin.saveResource("iblocks.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        blocks.clear();

        for (String key : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(key);
            if (sec == null) continue;

            Material material = Material.matchMaterial(sec.getString("material", "STONE"));
            if (material == null) material = Material.STONE;

            String world = sec.getString("world", "world");
            int x = sec.getInt("x");
            int y = sec.getInt("y");
            int z = sec.getInt("z");
            double cooldown = sec.getDouble("cooldown", 0.0);
            InteractionType type = InteractionType.fromString(sec.getString("interaction-type", "CLICK"));
            if (type == null) type = InteractionType.CLICK;

            String permission = sec.getString("permission", "none");
            List<String> commands = sec.getStringList("command");
            List<String> messages = sec.getStringList("message");

            InteractiveBlock block = new InteractiveBlock(key, material, world, x, y, z, cooldown, type, permission, commands, messages);
            blocks.put(key.toLowerCase(), block);
        }
    }

    public void saveBlocks() {
        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }

        for (InteractiveBlock block : blocks.values()) {
            ConfigurationSection sec = config.createSection(block.getName());
            sec.set("material", block.getMaterial().name());
            sec.set("world", block.getWorld());
            sec.set("x", block.getX());
            sec.set("y", block.getY());
            sec.set("z", block.getZ());
            sec.set("cooldown", block.getCooldown());
            sec.set("interaction-type", block.getInteractionType().name());
            sec.set("permission", block.getPermission());
            sec.set("command", block.getCommands());
            sec.set("message", block.getMessages());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nepodařilo se uložit iblocks.yml!");
            e.printStackTrace();
        }
    }

    public void createBlock(String name, Location loc) {
        InteractiveBlock block = new InteractiveBlock(
                name,
                loc.getBlock().getType(),
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ(),
                0.0,
                InteractionType.CLICK,
                "none",
                new ArrayList<>(),
                new ArrayList<>()
        );
        blocks.put(name.toLowerCase(), block);
        saveBlocks();
    }

    public boolean deleteBlock(String name) {
        if (blocks.remove(name.toLowerCase()) != null) {
            saveBlocks();
            return true;
        }
        return false;
    }

    public InteractiveBlock getBlockByName(String name) {
        return blocks.get(name.toLowerCase());
    }

    public InteractiveBlock getBlockAtLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        for (InteractiveBlock block : blocks.values()) {
            if (block.getWorld().equalsIgnoreCase(loc.getWorld().getName()) &&
                    block.getX() == loc.getBlockX() &&
                    block.getY() == loc.getBlockY() &&
                    block.getZ() == loc.getBlockZ()) {
                return block;
            }
        }
        return null;
    }

    public Collection<InteractiveBlock> getAllBlocks() {
        return blocks.values();
    }
}
