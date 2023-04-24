package main.commands;

import main.RowdyVotesP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static main.RowdyVotesP.*;

public class balance implements CommandExecutor {

    private final String wallet;

    public balance(String wallet) {
        this.wallet = wallet;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько игрок может использовать эту команду");
            return true;
        }
        if (args.length < 1) {
            int balance = getPlayerBalance(sender.getName());
            sender.sendMessage("§aНа вашем балансе " + balance + wallet);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "transfer" -> {
                if (args.length != 3) {
                    sender.sendMessage("§cИспользование: /balance transfer <игрок> <сумма>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                String target_name = args[1];
                if (target == null && !playerBalances.containsKey(target_name)) {
                    sender.sendMessage("§cИгрок не найден");
                    return true;
                }
                if (sender == target) {
                    sender.sendMessage("§cНельзя отправлять " + wallet + " самому себе");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cДолжно быть указано целое число");
                    return true;
                }
                if (amount <= 0) {
                    sender.sendMessage("§cЧисло должно быть положительным");
                    return true;
                }
                if (getPlayerBalance(sender.getName()) < amount) {
                    sender.sendMessage("§cУ тебя недостаточно " + wallet);
                    return true;
                }
                removePlayerBalance(sender.getName(), amount);
                addPlayerBalance(target_name, amount);
                sender.sendMessage("§aТы перевел " + amount + wallet + " игроку " + target_name);
                if (target != null) {
                    target.sendMessage("§aТебе было переведено " + amount + wallet + " от игрока " + sender.getName());
                }
            }
            case "set" -> {
                if (!sender.hasPermission("votes.balance.set")) {
                    sender.sendMessage("Недостаточно прав!");
                    return false;
                }
                if (args.length == 3) {
                    RowdyVotesP.setPlayerBalance(args[1], Integer.parseInt(args[2]));
                    sender.sendMessage("§aВы установили баланс " + Integer.parseInt(args[2]) + wallet + " игроку " + args[1]);
                    return true;
                } else {
                    sender.sendMessage("§cИспользование: /balance set <игрок> <баланс>");
                    return false;
                }
            }
        }
        return true;
    }
}
