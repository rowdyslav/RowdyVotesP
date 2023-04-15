package main.listeners;

import main.RowdyVoteP;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static main.RowdyVoteP.setPlayerBalance;

public class join_leave implements Listener {

    private final File balancesFile;
    private final HashMap<String, Integer> playerBalances;
    private final YamlConfiguration balancesConfig;

    public join_leave(File balancesFile, YamlConfiguration balancesConfig, HashMap<String, Integer> playerBalances) {
        this.balancesFile = balancesFile;
        this.balancesConfig = balancesConfig;
        this.playerBalances = playerBalances;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        setPlayerBalance(joiner.getName(), (Integer) balancesConfig.get(joiner.getName(), 0));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quiter = event.getPlayer();
        balancesConfig.set(quiter.getName(), playerBalances.getOrDefault(quiter.getName(), 0));
        try {
            balancesConfig.save(balancesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}