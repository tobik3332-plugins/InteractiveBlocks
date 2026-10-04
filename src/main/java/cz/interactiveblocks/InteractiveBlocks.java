package cz.interactiveblocks;

import cz.interactiveblocks.command.IBlocksCommand;
import cz.interactiveblocks.config.BlockManager;
import cz.interactiveblocks.config.ConfigManager;
import cz.interactiveblocks.listener.BlockProtectionListener;
import cz.interactiveblocks.listener.InteractionListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class InteractiveBlocks extends JavaPlugin {

    private ConfigManager configManager;
    private BlockManager blockManager;

    @Override
    public void onEnable() {
        // Načtení konfigurace
        this.configManager = new ConfigManager(this);
        this.blockManager = new BlockManager(this);

        // Registrace příkazů a TabCompleteru
        if (getCommand("iblocks") != null) {
            IBlocksCommand cmdExecutor = new IBlocksCommand(this);
            getCommand("iblocks").setExecutor(cmdExecutor);
            getCommand("iblocks").setTabCompleter(cmdExecutor);
        }

        // Registrace listenerů
        getServer().getPluginManager().registerEvents(new InteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);

        getLogger().info("InteractiveBlocks byl uspesne aktivovan!");
    }

    @Override
    public void onDisable() {
        if (blockManager != null) {
            blockManager.saveBlocks();
        }
        getLogger().info("InteractiveBlocks byl uspesne deaktivovan!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }
}
