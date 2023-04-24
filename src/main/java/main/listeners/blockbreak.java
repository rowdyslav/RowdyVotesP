package main.listeners;

import main.RowdyVotesP;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;

public class blockbreak implements Listener {

    private final YamlConfiguration configConfig;

    public blockbreak(YamlConfiguration configConfig) {
        this.configConfig = configConfig;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material blockType = event.getBlock().getType();
        boolean silkTouchMain = event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH);
        boolean silkTouchOff = event.getPlayer().getInventory().getItemInOffHand().containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH);
        Map<String, Object> blockRewards = configConfig.getConfigurationSection("block-rewards").getValues(false);

        if (blockRewards.containsKey(blockType.toString()) && !silkTouchMain && !silkTouchOff) {
            Player player = event.getPlayer();
            int amount = (int) blockRewards.get(blockType.toString());
            RowdyVotesP.addPlayerBalance(player.getName(), amount);
        }
    }
}
