package main.tabs;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class balance_tab implements TabCompleter {

    List<String> completions = new ArrayList<>();
    List<String> off = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (completions.isEmpty()) {
            completions.add("transfer");
            completions.add("set");
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
        if (args.length == 2) {
            for (String a : getPlayerNames()) {
                if (a.toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        }
        return off;
    }

    private List<String> getPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
        Bukkit.getServer().getOnlinePlayers().toArray(players);
        for (int i = 0; i < players.length; i++) {
            playerNames.add(players[i].getName());
        }
        return playerNames;
    }
}