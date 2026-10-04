package cz.interactiveblocks.command;

import cz.interactiveblocks.InteractiveBlocks;
import cz.interactiveblocks.model.InteractionType;
import cz.interactiveblocks.model.InteractiveBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IBlocksTabCompleter implements TabCompleter {

    private final InteractiveBlocks plugin;

    public IBlocksTabCompleter(InteractiveBlocks plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("iblocks.admin")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return filter(Arrays.asList("create", "delete", "edit", "list", "reload"), args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("delete") || sub.equals("edit")) {
                List<String> blockNames = plugin.getBlockManager().getAllBlocks()
                        .stream().map(InteractiveBlock::getName).collect(Collectors.toList());
                return filter(blockNames, args[1]);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("edit")) {
            return filter(Arrays.asList(
                    "permission", "cooldown", "interaction-type",
                    "addcommand", "deletecommand", "addmessage", "deletemessage"
            ), args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("edit")) {
            String editType = args[2].toLowerCase();
            InteractiveBlock block = plugin.getBlockManager().getBlockByName(args[1]);

            if (editType.equals("interaction-type")) {
                List<String> types = Arrays.stream(InteractionType.values()).map(Enum::name).collect(Collectors.toList());
                return filter(types, args[3]);
            } else if (editType.equals("addcommand")) {
                return filter(Arrays.asList("player", "console"), args[3]);
            } else if (editType.equals("addmessage")) {
                return filter(Arrays.asList("message", "broadcast"), args[3]);
            } else if (editType.equals("deletecommand") && block != null) {
                List<String> ids = new ArrayList<>();
                for (int i = 1; i <= block.getCommands().size(); i++) {
                    ids.add(String.valueOf(i));
                }
                return filter(ids, args[3]);
            } else if (editType.equals("deletemessage") && block != null) {
                List<String> ids = new ArrayList<>();
                for (int i = 1; i <= block.getMessages().size(); i++) {
                    ids.add(String.valueOf(i));
                }
                return filter(ids, args[3]);
            }
        }

        return completions;
    }

    private List<String> filter(List<String> list, String input) {
        return list.stream()
                .filter(item -> item.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
