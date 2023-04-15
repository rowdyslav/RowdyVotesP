package main.commands;

import main.RowdyVoteP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class vote implements CommandExecutor {

    private RowdyVoteP plugin;

    public vote(RowdyVoteP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Использование: /vote <id голосования> <yes/no> <сумма>");
            return true;
        }

        int voteId;
        try {
            voteId = Integer.parseInt(args[0]); // Получаем id голосования из аргументов команды
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Некорректный id голосования");
            return true;
        }

        String voteOption = args[1].toLowerCase(); // Получаем вариант голоса (yes или no) из аргументов команды
        if (!voteOption.equals("yes") && !voteOption.equals("no")) {
            sender.sendMessage(ChatColor.RED + "Некорректный вариант голоса. Используйте 'yes' или 'no'.");
            return true;
        }

        int voteAmount;
        try {
            voteAmount = Integer.parseInt(args[2]); // Получаем сумму голоса из аргументов команды
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Некорректная сумма голоса");
            return true;
        }

        File votesFile = new File(plugin.getDataFolder(), "votes.yml");
        YamlConfiguration votesConfig = YamlConfiguration.loadConfiguration(votesFile);

        if (!votesConfig.contains(String.valueOf(voteId))) {
            sender.sendMessage(ChatColor.RED + "Голосование с указанным id не найдено");
            return true;
        }

        String voteStatus = votesConfig.getString(voteId + ".status");
        if (!voteStatus.equals("available")) {
            sender.sendMessage(ChatColor.RED + "Голосование уже завершено");
            return true;
        }

        // Обработка голоса и обновление значения в votes.yml
        int voteValue = votesConfig.getInt(voteId + "." + voteOption);
        votesConfig.set(voteId + "." + voteOption, voteValue + voteAmount);
        plugin.saveYamlConfig(votesConfig, votesFile);

        sender.sendMessage(ChatColor.GREEN + "Ваш голос успешно учтен");

        return true;
    }
}
