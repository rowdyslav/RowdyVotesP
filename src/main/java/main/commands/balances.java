package main.commands;

import main.RowdyVoteP;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class balances implements CommandExecutor {

    private final HashMap<String, Integer> playerBalances;
    private final String wallet;

    public balances(HashMap<String, Integer> playerBalances, String wallet) {
        this.playerBalances = playerBalances;
        this.wallet = wallet;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("votes.balances")) {
            sender.sendMessage("Недостаточно прав!");
            return false;
        }

        int page = 1; // Номер текущей страницы
        int pageSize = 10; // Количество записей на одной странице
        int totalPages = (int) Math.ceil((double) playerBalances.size() / pageSize); // Общее количество страниц

        if (args.length == 1) {
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
        // Сортируем игроков по убыванию баланса и преобразуем в список
        List<Map.Entry<String, Integer>> playerEntries = playerBalances.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .toList();

        // Выводим список игроков с их балансами на заданной странице
        sender.sendMessage(String.format("%sСтраница %d из %d:", ChatColor.GOLD, page, totalPages));

        int startIndex = (page - 1) * pageSize; // Индекс первой записи на странице
        int endIndex = Math.min(startIndex + pageSize, playerEntries.size()); // Индекс последней записи на странице

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, Integer> entry = playerEntries.get(i);
            String name = entry.getKey();
            int balance = entry.getValue();
            String message = ChatColor.GREEN + Integer.toString(balance) + ChatColor.RESET + wallet + ChatColor.RESET + ' ' + ChatColor.YELLOW + name;
            sender.sendMessage(message);
        }
        if (!(sender instanceof Player player)) {
            return true;
        }
        // Добавляем кнопки для листания страниц
        TextComponent leftButton = new TextComponent("<<<");
        ClickEvent leftClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/balances " + (page - 1));
        leftButton.setClickEvent(leftClick);

        TextComponent rightButton = new TextComponent(">>>");
        ClickEvent rightClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/balances " + (page + 1));
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
        return true;
    }
}