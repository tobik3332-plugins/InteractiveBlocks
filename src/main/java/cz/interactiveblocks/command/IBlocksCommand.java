package cz.interactiveblocks.command;

import cz.interactiveblocks.InteractiveBlocks;
import cz.interactiveblocks.model.InteractionType;
import cz.interactiveblocks.model.InteractiveBlock;
import cz.interactiveblocks.util.ColorUtils;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IBlocksCommand implements CommandExecutor, TabCompleter {

    private final InteractiveBlocks plugin;

    public IBlocksCommand(InteractiveBlocks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("iblocks.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                plugin.getConfigManager().reloadConfig();
                plugin.getBlockManager().loadBlocks();
                sender.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
                break;

            case "list":
                sender.sendMessage(plugin.getConfigManager().getRawMessage("list-header"));
                var blocks = plugin.getBlockManager().getAllBlocks();
                if (blocks.isEmpty()) {
                    sender.sendMessage(plugin.getConfigManager().getRawMessage("list-empty"));
                } else {
                    for (InteractiveBlock b : blocks) {
                        String msg = plugin.getConfigManager().getRawMessage("list-format")
                                .replace("%name%", b.getName())
                                .replace("%material%", b.getMaterial().name())
                                .replace("%world%", b.getWorld())
                                .replace("%x%", String.valueOf(b.getX()))
                                .replace("%y%", String.valueOf(b.getY()))
                                .replace("%z%", String.valueOf(b.getZ()));
                        sender.sendMessage(msg);
                    }
                }
                sender.sendMessage(plugin.getConfigManager().getRawMessage("list-footer"));
                break;

            case "create":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("only-player"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.colorize("&cPoužití: /iblocks create <název>"));
                    return true;
                }
                String name = args[1];
                if (plugin.getBlockManager().getBlockByName(name) != null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("block-already-exists"));
                    return true;
                }
                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("target-not-block"));
                    return true;
                }
                plugin.getBlockManager().createBlock(name, targetBlock.getLocation());
                player.sendMessage(plugin.getConfigManager().getMessage("block-created").replace("%name%", name));
                break;

            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.colorize("&cPoužití: /iblocks delete <název>"));
                    return true;
                }
                String delName = args[1];
                if (plugin.getBlockManager().deleteBlock(delName)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("block-deleted").replace("%name%", delName));
                } else {
                    sender.sendMessage(plugin.getConfigManager().getMessage("block-not-found"));
                }
                break;

            case "edit":
                handleEdit(sender, args);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sendEditHelp(sender);
            return;
        }

        String blockName = args[1];
        InteractiveBlock block = plugin.getBlockManager().getBlockByName(blockName);
        if (block == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("block-not-found"));
            return;
        }

        String action = args[2].toLowerCase();

        switch (action) {
            case "permission":
                String perm = args[3];
                block.setPermission(perm);
                plugin.getBlockManager().saveBlocks();
                sender.sendMessage(plugin.getConfigManager().getMessage("edit-permission")
                        .replace("%name%", block.getName()).replace("%value%", perm));
                break;

            case "cooldown":
                try {
                    double cd = Double.parseDouble(args[3]);
                    block.setCooldown(cd);
                    plugin.getBlockManager().saveBlocks();
                    sender.sendMessage(plugin.getConfigManager().getMessage("edit-cooldown")
                            .replace("%name%", block.getName()).replace("%value%", String.valueOf(cd)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid-number"));
                }
                break;

            case "interaction-type":
                InteractionType type = InteractionType.fromString(args[3]);
                if (type == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid-interaction-type"));
                    return;
                }
                block.setInteractionType(type);
                plugin.getBlockManager().saveBlocks();
                sender.sendMessage(plugin.getConfigManager().getMessage("edit-type")
                        .replace("%name%", block.getName()).replace("%value%", type.name()));
                break;

            case "addcommand":
                if (args.length < 5) {
                    sender.sendMessage(ColorUtils.colorize("&cPoužití: /iblocks edit <název> addcommand <player/console> <příkaz>"));
                    return;
                }
                String cmdType = args[3].toLowerCase();
                if (!cmdType.equals("player") && !cmdType.equals("console")) {
                    sender.sendMessage(ColorUtils.colorize("&cTyp příkazu musí být 'player' nebo 'console'!"));
                    return;
                }
                String cmdText = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                String fullCmd = "[" + cmdType + "] " + cmdText;
                block.getCommands().add(fullCmd);
                plugin.getBlockManager().saveBlocks();
                sender.sendMessage(plugin.getConfigManager().getMessage("add-command-success")
                        .replace("%name%", block.getName()).replace("%type%", cmdType).replace("%command%", cmdText));
                break;

            case "deletecommand":
                try {
                    int index = Integer.parseInt(args[3]) - 1;
                    if (index < 0 || index >= block.getCommands().size()) {
                        sender.sendMessage(plugin.getConfigManager().getMessage("invalid-index"));
                        return;
                    }
                    block.getCommands().remove(index);
                    plugin.getBlockManager().saveBlocks();
                    sender.sendMessage(plugin.getConfigManager().getMessage("delete-command-success")
                            .replace("%name%", block.getName()).replace("%id%", String.valueOf(index + 1)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid-number"));
                }
                break;

            case "addmessage":
                if (args.length < 5) {
                    sender.sendMessage(ColorUtils.colorize("&cPoužití: /iblocks edit <název> addmessage <message/broadcast> <zpráva>"));
                    return;
                }
                String msgType = args[3].toLowerCase();
                if (!msgType.equals("message") && !msgType.equals("broadcast")) {
                    sender.sendMessage(ColorUtils.colorize("&cTyp zprávy musí být 'message' nebo 'broadcast'!"));
                    return;
                }
                String msgText = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                String fullMsg = "[" + msgType + "] " + msgText;
                block.getMessages().add(fullMsg);
                plugin.getBlockManager().saveBlocks();
                sender.sendMessage(plugin.getConfigManager().getMessage("add-message-success")
                        .replace("%name%", block.getName()).replace("%type%", msgType).replace("%message%", msgText));
                break;

            case "deletemessage":
                try {
                    int index = Integer.parseInt(args[3]) - 1;
                    if (index < 0 || index >= block.getMessages().size()) {
                        sender.sendMessage(plugin.getConfigManager().getMessage("invalid-index"));
                        return;
                    }
                    block.getMessages().remove(index);
                    plugin.getBlockManager().saveBlocks();
                    sender.sendMessage(plugin.getConfigManager().getMessage("delete-message-success")
                            .replace("%name%", block.getName()).replace("%id%", String.valueOf(index + 1)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid-number"));
                }
                break;

            default:
                sendEditHelp(sender);
                break;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&8&m--------&r &bInteractiveBlocks Nápověda &8&m--------"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks create <název> &7- Vytvoří blok z pohledu"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks delete <název> &7- Smaže blok"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks list &7- Seznam bloků"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks edit <název> ... &7- Úprava bloku"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks reload &7- Znovu načte konfiguraci"));
        sender.sendMessage(ColorUtils.colorize("&8&m---------------------------------------"));
    }

    private void sendEditHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&8&m--------&r &bInteractiveBlocks Editace &8&m--------"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks edit <název> permission <permise>"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks edit <název> cooldown <sekundy>"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks edit <název> interaction-type <LEFT_CLICK|RIGHT_CLICK|CLICK>"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks edit <název> addcommand <player|console> <příkaz>"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks edit <název> deletecommand <id>"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks edit <název> addmessage <message|broadcast> <zpráva>"));
        sender.sendMessage(ColorUtils.colorize("&e/iblocks edit <název> deletemessage <id>"));
        sender.sendMessage(ColorUtils.colorize("&8&m---------------------------------------"));
    }

    // ========================================================
    //                     TAB COMPLETER
    // ========================================================
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("iblocks.admin")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("create", "delete", "edit", "list", "reload"));
            return filterPrefix(completions, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("delete") || sub.equals("edit")) {
                for (InteractiveBlock b : plugin.getBlockManager().getAllBlocks()) {
                    completions.add(b.getName());
                }
                return filterPrefix(completions, args[1]);
            }
            if (sub.equals("create")) {
                return List.of("<název_bloku>");
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("edit")) {
            completions.addAll(List.of("permission", "cooldown", "interaction-type", "addcommand", "deletecommand", "addmessage", "deletemessage"));
            return filterPrefix(completions, args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("edit")) {
            String editType = args[2].toLowerCase();
            switch (editType) {
                case "interaction-type":
                    completions.addAll(List.of("LEFT_CLICK", "RIGHT_CLICK", "CLICK"));
                    break;
                case "addcommand":
                    completions.addAll(List.of("player", "console"));
                    break;
                case "addmessage":
                    completions.addAll(List.of("message", "broadcast"));
                    break;
                case "permission":
                    completions.add("none");
                    break;
                case "cooldown":
                    completions.addAll(List.of("0.0", "0.5", "1.0", "5.0"));
                    break;
                case "deletecommand":
                case "deletemessage":
                    InteractiveBlock block = plugin.getBlockManager().getBlockByName(args[1]);
                    if (block != null) {
                        int count = editType.equals("deletecommand") ? block.getCommands().size() : block.getMessages().size();
                        for (int i = 1; i <= count; i++) {
                            completions.add(String.valueOf(i));
                        }
                    }
                    break;
            }
            return filterPrefix(completions, args[3]);
        }

        return List.of();
    }

    private List<String> filterPrefix(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
