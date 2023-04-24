package main;

import main.commands.balance;
import main.commands.balances;
import main.commands.vote;
import main.commands.votes;
import main.counters.VoteFee;
import main.listeners.blockbreak;
import main.listeners.join_leave;
import main.tabs.balance_tab;
import main.tabs.balances_tab;
import main.tabs.vote_tab;
import main.tabs.votes_tab;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class RowdyVotesP extends JavaPlugin {

    public static HashMap<String, Integer> playerBalances;
    public String wallet;
    public File balancesFile;
    public static File configFile;
    public static File votesFile;
    public YamlConfiguration balancesConfig;
    public static YamlConfiguration configConfig;
    public static YamlConfiguration votesConfig;


    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        playerBalances = new HashMap<>();

        balancesFile = new File(getDataFolder(), "balances.yml");
        if (!balancesFile.exists()) {
            try {
                balancesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        balancesConfig = YamlConfiguration.loadConfiguration(balancesFile);
        for (String playerName : balancesConfig.getKeys(false)) {
            int balance = balancesConfig.getInt(playerName);
            playerBalances.put(playerName, balance);
        }

        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                configConfig = YamlConfiguration.loadConfiguration(configFile);
                // Устанавливаем значение wallet по умолчанию
                defaultConfig();
                configConfig.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            configConfig = YamlConfiguration.loadConfiguration(configFile);
        }

        votesFile = new File(getDataFolder(), "votes.yml");
        if (!votesFile.exists()) {
            try {
                votesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        votesConfig = YamlConfiguration.loadConfiguration(votesFile);
        ConfigurationSection votesSection = votesConfig.getConfigurationSection("");
        // Получаем значение wallet из config.yml
        wallet = configConfig.getString("wallet", "$");

        VoteFee feeCounter = new VoteFee(playerBalances);
        feeCounter.runTaskTimer(this, 0, 24000);

        getServer().getPluginManager().registerEvents(new blockbreak(configConfig), this);
        getServer().getPluginManager().registerEvents(new join_leave(balancesFile, balancesConfig, playerBalances), this);
        getCommand("balance").setExecutor(new balance(wallet));
        getCommand("balances").setExecutor(new balances(playerBalances, wallet));
        getCommand("votes").setExecutor(new votes(this, feeCounter, wallet, votesConfig, votesSection));
        getCommand("vote").setExecutor(new vote(this, wallet));

        getCommand("balance").setTabCompleter(new balance_tab());
        getCommand("balances").setTabCompleter(new balances_tab());
        getCommand("vote").setTabCompleter(new vote_tab(this));
        getCommand("votes").setTabCompleter(new votes_tab());
    }

    @Override
    public void onDisable() {
        balancesFile = new File(getDataFolder(), "balances.yml");
        if (!balancesFile.exists()) {
            try {
                balancesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        balancesConfig = YamlConfiguration.loadConfiguration(balancesFile);
        for (String player : playerBalances.keySet()) {
            balancesConfig.set(player, playerBalances.getOrDefault(player, 0));
        }
        saveYamlConfig(balancesConfig, balancesFile);

        getServer().getScheduler().cancelTasks(this);
    }


    public static int getPlayerBalance(String player) {
        return playerBalances.getOrDefault(player, 0);
    }

    public static void removePlayerBalance(String player, int amount) {
        int currentBalance = playerBalances.getOrDefault(player, 0);
        playerBalances.put(player, currentBalance - amount);
    }

    public static void addPlayerBalance(String player, int amount) {
        int currentBalance = playerBalances.getOrDefault(player, 0);
        playerBalances.put(player, currentBalance + amount);
    }

    public static void setPlayerBalance(String player, int amount) {
        playerBalances.put(player, amount);
    }

    public void saveYamlConfig(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void defaultConfig() {
        configConfig.set("wallet", "$");
        configConfig.set("info", "Это похоже на еще не настроенную информацию о плагине.. странно");
        configConfig.set("minutes_on_vote", 2);
        configConfig.set("block-rewards.DIAMOND_ORE", 1);
        configConfig.set("block-rewards.DEEPSLATE_DIAMOND_ORE", 1);
    }
}