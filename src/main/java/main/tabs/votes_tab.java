package main.tabs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class votes_tab implements TabCompleter {

    List<String> completions = new ArrayList<>();
    List<String> subcompletions = new ArrayList<>();
    List<String> off = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (completions.isEmpty()) {
            completions.add("create");
            completions.add("list");
        }
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String a : completions) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        }
        subcompletions.add("available");
        subcompletions.add("finished");
        if (args.length == 2 && args[0].equals("list")) {
            for (String a : subcompletions) {
                if (a.toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        }
        return off;
    }
}
