package main.tabs;

import main.RowdyVotesP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class vote_tab implements TabCompleter {

    private final RowdyVotesP plugin;

    public vote_tab(RowdyVotesP plugin) {
        this.plugin = plugin;
    }

    List<String> voteids = new ArrayList<>();
    List<String> vars = new ArrayList<>();
    List<String> off = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        File votesFile = new File(plugin.getDataFolder(), "votes.yml");
        if (!votesFile.exists()) {
            try {
                votesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration votesConfig = YamlConfiguration.loadConfiguration(votesFile);
        ConfigurationSection votesSection = votesConfig.getConfigurationSection("");

        List<String> result = new ArrayList<>();

        if (votesSection != null) {
            Set<String> keys = votesSection.getKeys(false);
            for (String key : keys) {
                if (votesSection.getString(key + ".status").equalsIgnoreCase("available")) {
                    voteids.add(key);
                }
            }
        }

        if (args.length == 1) {
            for (String a : voteids) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        }

        vars.add("yes");
        vars.add("no");

        if (args.length == 2) {
            for (String a : vars) {
                if (a.toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        }
        return off;
    }

}
