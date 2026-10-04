package cz.interactiveblocks.listener;

import cz.interactiveblocks.InteractiveBlocks;
import cz.interactiveblocks.model.InteractionType;
import cz.interactiveblocks.model.InteractiveBlock;
import cz.interactiveblocks.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractionListener implements Listener {

    private final InteractiveBlocks plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public InteractionListener(InteractiveBlocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        InteractiveBlock iblock = plugin.getBlockManager().getBlockAtLocation(clickedBlock.getLocation());
        if (iblock == null) return;

        Player player = event.getPlayer();

        // Kontrola typu kliknutí
        Action action = event.getAction();
        InteractionType type = iblock.getInteractionType();

        if (type == InteractionType.LEFT_CLICK && action != Action.LEFT_CLICK_BLOCK) return;
        if (type == InteractionType.RIGHT_CLICK && action != Action.RIGHT_CLICK_BLOCK) return;
        if (type == InteractionType.CLICK && (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK)) return;

        // Kontrola permise
        String perm = iblock.getPermission();
        if (perm != null && !perm.equalsIgnoreCase("none") && !player.hasPermission(perm)) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-block-permission"));
            return;
        }

        // Kontrola Cooldownu
        if (iblock.getCooldown() > 0) {
            long lastUse = getCooldown(player.getUniqueId(), iblock.getName());
            long timePassed = (System.currentTimeMillis() - lastUse) / 1000;
            if (timePassed < iblock.getCooldown()) {
                long timeLeft = iblock.getCooldown() - timePassed;
                player.sendMessage(plugin.getConfigManager().getMessage("cooldown").replace("%time%", String.valueOf(timeLeft)));
                return;
            }
        }

        // Zaznamenat nový cooldown
        setCooldown(player.getUniqueId(), iblock.getName());

        // Vykonání Příkazů
        for (String cmd : iblock.getCommands()) {
            executeCommand(player, cmd);
        }

        // Vykonání Zpráv
        for (String msg : iblock.getMessages()) {
            executeMessage(player, msg);
        }
    }

    private void executeCommand(Player player, String raw) {
        String parsed = raw.replace("%player%", player.getName());

        if (parsed.startsWith("[console] ")) {
            String command = parsed.substring(10);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else if (parsed.startsWith("[player] ")) {
            String command = parsed.substring(9);
            player.performCommand(command);
        }
    }

    private void executeMessage(Player player, String raw) {
        String parsed = raw.replace("%player%", player.getName());

        if (parsed.startsWith("[message] ")) {
            String text = parsed.substring(10);
            player.sendMessage(ColorUtils.parseComponent(text));
        } else if (parsed.startsWith("[broadcast] ")) {
            String text = parsed.substring(12);
            Bukkit.broadcast(ColorUtils.parseComponent(text));
        }
    }

    private long getCooldown(UUID uuid, String blockName) {
        return cooldowns.getOrDefault(uuid, new HashMap<>()).getOrDefault(blockName.toLowerCase(), 0L);
    }

    private void setCooldown(UUID uuid, String blockName) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(blockName.toLowerCase(), System.currentTimeMillis());
    }
}
