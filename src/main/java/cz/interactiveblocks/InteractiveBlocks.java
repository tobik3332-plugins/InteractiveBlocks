package cz.interactiveblocks;

import cz.interactiveblocks.command.IBlocksCommand;
import cz.interactiveblocks.command.IBlocksTabCompleter;
import cz.interactiveblocks.config.BlockManager;
import cz.interactiveblocks.config.ConfigManager;
import cz.interactiveblocks.listener.InteractionListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class InteractiveBlocks extends JavaPlugin {

    private ConfigManager configManager;
    private BlockManager blockManager;

    @Override
    public void onEnable() {
        // Načtení Konfigurací
        this.configManager = new ConfigManager(this);
        this.blockManager = new BlockManager(this);

        // Registrace Listeneru
        getServer().getPluginManager().registerEvents(new InteractionListener(this), this);

        // Registrace Příkazů
        IBlocksCommand cmdExecutor = new IBlocksCommand(this);
        IBlocksTabCompleter tabCompleter = new IBlocksTabCompleter(this);

        if (getCommand("iblocks") != null) {
            getCommand("iblocks").setExecutor(cmdExecutor);
            getCommand("iblocks").setTabCompleter(tabCompleter);
        }

        getLogger().info("InteractiveBlocks plugin byl uspesne nacten a aktivovan!");
    }

    @Override
    public void onDisable() {
        if (blockManager != null) {
            blockManager.saveBlocks();
        }
        getLogger().info("InteractiveBlocks plugin byl uspesne vypnut.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public BlockManager getBlockManager() { return blockManager; }
}
