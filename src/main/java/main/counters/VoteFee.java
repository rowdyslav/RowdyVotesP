package main.counters;

import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;

public class VoteFee extends BukkitRunnable {
    private HashMap<String, Integer> hashMap;
    private int average;

    public VoteFee(HashMap<String, Integer> hashMap) {
        this.hashMap = hashMap;
    }

    @Override
    public void run() {
        int sum = 0;
        int count = 0;
        for (Integer value : hashMap.values()) {
            sum += value;
            count++;
        }
        double avg = count > 0 ? (double) sum / count : 0.0;
        average = (int) Math.round(avg * 0.8);
    }

    public int getFee() {
        return average;
    }
}
