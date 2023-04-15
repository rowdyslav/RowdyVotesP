package main.commands;

import main.RowdyVoteP;
import main.counters.VoteFee;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.List;
import java.util.Set;

import static main.RowdyVoteP.*;

public class votes implements CommandExecutor {

    private final VoteFee feeCounter;
    private final String wallet;
    private final RowdyVoteP plugin;
    public final YamlConfiguration votesConfig;
    private final ConfigurationSection votesSection;

    public votes(RowdyVoteP plugin, VoteFee feeCounter, String wallet, YamlConfiguration votesConfig, ConfigurationSection votesSection) {
        this.plugin = plugin;
        this.feeCounter = feeCounter;
        this.wallet = wallet;
        this.votesConfig = votesConfig;
        this.votesSection = votesSection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда может быть выполнена только игроком");
            return false;
        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /votes create {текст}");
            return false;
        }
        int fee = feeCounter.getFee();
        switch (args[0].toLowerCase()) {
            case "info":
                player.sendMessage(plugin.getConfig().getString("custom-description"));
                player.sendMessage("Текущая плата за голосование: " + feeCounter.getFee());
                return true;
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /votes create <текст>");
                    return true;
                }
                String voteText = args[1];
                if (getPlayerBalance(player.getName()) < fee) {
                    player.sendMessage(ChatColor.RED + "У вас недостаточно средств для создания голосования");
                    return true;
                }
                removePlayerBalance(player.getName(), fee);
                player.sendMessage(ChatColor.GREEN + "Голосование успешно создано. Плата за голосование " + fee + wallet + " списана с вашего баланса");

                // Создание новой секции в votes.yml
                File votesFile = new File(plugin.getDataFolder(), "votes.yml");
                YamlConfiguration votesConfig = YamlConfiguration.loadConfiguration(votesFile);
                int voteIdx = votesConfig.getKeys(false).size(); // Получаем порядковый номер голосования
                votesConfig.set(voteIdx + ".text", voteText); // Устанавливаем текст голосования
                votesConfig.set(voteIdx + ".author", sender.getName());
                votesConfig.set(voteIdx + ".status", "available"); // Устанавливаем начальный статус "available"
                votesConfig.set(voteIdx + ".yes", 0); // Устанавливаем начальное значение "yes" в 0
                votesConfig.set(voteIdx + ".no", 0); // Устанавливаем начальное значение "no" в 0
                plugin.saveYamlConfig(votesConfig, votesFile); // Сохраняем файл votes.yml

                // Запуск таймера
                BukkitScheduler scheduler = plugin.getServer().getScheduler();
                int taskId = scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        // Код, который будет выполняться после окончания таймера
                        votesConfig.set(voteIdx + ".status", "finished"); // Изменяем статус голосования на "finished"
                        plugin.saveYamlConfig(votesConfig, votesFile); // Сохраняем файл votes.yml
                    }
                }, configConfig.getInt("minutes_on_vote") * 1200L);

                Bukkit.broadcastMessage("§bГолосование - §l§f" + voteText);
                Bukkit.broadcastMessage("§b§aДа §r§o/vote " + voteIdx + " yes <сумма>");
                Bukkit.broadcastMessage("§b§cНет §r§o/vote " + voteIdx + " no <сумма>");
                return true;
            case "list":
                String status = args.length > 1 ? args[1].toLowerCase() : "available"; // Получаем статус из аргументов команды или используем значение "available" по умолчанию
                int page = 1; // Номер текущей страницы
                int pageSize = 10; // Количество записей на одной странице

                // Получаем данные из файла votes.yml
                Set<String> voteIds = votesSection.getKeys(false); // Получаем все айди голосований

                // Фильтруем голосования по статусу и преобразуем в список
                List<String> filteredVoteIds = voteIds.stream()
                        .filter(voteId -> votesSection.getString(voteId + ".status").equalsIgnoreCase(status))
                        .sorted()
                        .toList();

                int totalPages = (int) Math.ceil((double) filteredVoteIds.size() / pageSize); // Общее количество страниц

                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Неверный номер страницы!");
                        return true;
                    }

                    if (page < 1 || page > totalPages) {
                        sender.sendMessage("Номер страницы вне допустимого диапазона!");
                        return true;
                    }
                }

                // Выводим список голосований на заданной странице
                sender.sendMessage(String.format("%sСтраница %d из %d:", ChatColor.GOLD, page, totalPages));

                int startIndex = (page - 1) * pageSize; // Индекс первой записи на странице
                int endIndex = Math.min(startIndex + pageSize, filteredVoteIds.size()); // Индекс последней записи на странице

                for (int i = startIndex; i < endIndex; i++) {
                    String voteId = filteredVoteIds.get(i);
                    String text = votesSection.getString(voteId + ".text");
                    String author = votesSection.getString(voteId + ".author");
                    int yes = votesSection.getInt(voteId + ".yes");
                    int no = votesSection.getInt(voteId + ".no");

                    String message = String.format("%s%s - %s\n%s\n%sДа %d  Нет %d%s",
                            ChatColor.YELLOW, voteId, text, author, ChatColor.GREEN, yes, no, wallet);
                    sender.sendMessage(message);
                }

                // Добавляем кнопки для листания страниц
                TextComponent leftButton = new TextComponent("<<<");
                ClickEvent leftClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/votes list " + (page - 1));
                leftButton.setClickEvent(leftClick);

                TextComponent rightButton = new TextComponent(">>>");
                ClickEvent rightClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/votes list " + (page + 1));
                rightButton.setClickEvent(rightClick);

                // Добавляем форматирование кнопок
                leftButton.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                leftButton.setBold(true);
                rightButton.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                rightButton.setBold(true);

                // Добавляем подсказки при наведении на кнопки
                leftButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Предыдущая страница").create()));
                rightButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Следующая страница").create()));

                TextComponent space_14 = new TextComponent("              ");
                TextComponent space_11 = new TextComponent("           ");
                TextComponent itog = new TextComponent();
                if (page == 1) {
                    itog.addExtra(space_14);
                    itog.addExtra(rightButton);
                } else if (page < totalPages) {
                    itog.addExtra(leftButton);
                    itog.addExtra(space_11);
                    itog.addExtra(rightButton);
                } else {
                    itog.addExtra(leftButton);
                    itog.addExtra(space_14);
                }
                sender.spigot().sendMessage(itog);
        }
        return false;
    }
}