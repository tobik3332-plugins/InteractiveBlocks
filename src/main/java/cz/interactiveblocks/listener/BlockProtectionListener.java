package cz.interactiveblocks.listener;

import cz.interactiveblocks.InteractiveBlocks;
import cz.interactiveblocks.model.InteractiveBlock;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockProtectionListener implements Listener {

    private final InteractiveBlocks plugin;

    public BlockProtectionListener(InteractiveBlocks plugin) {
        this.plugin = plugin;
    }

    // Ochrana před zničením hráčem (Survival i Creative)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInteractiveBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // Ochrana před výbuchy (TNT, Creeper, Bed v Netheru atd.)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isInteractiveBlock);
    }

    // Ochrana před výbuchy bloků (např. Respawn Anchor)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::isInteractiveBlock);
    }

    // Ochrana před shořením od ohně
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (isInteractiveBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // Ochrana před posunutím pístem
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isInteractiveBlock(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Ochrana před přitažením pístem
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isInteractiveBlock(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Ochrana před zničením od spadajících bloků nebo entit (např. Anvil, Dragon, Wither)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isInteractiveBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean isInteractiveBlock(Block block) {
        if (block == null) return false;
        InteractiveBlock iBlock = plugin.getBlockManager().getBlockAtLocation(block.getLocation());
        return iBlock != null;
    }
}
