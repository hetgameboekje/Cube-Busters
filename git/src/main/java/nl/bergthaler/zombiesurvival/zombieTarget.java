package com.example.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.util.Comparator;
import java.util.List;

public class zombieTarget {
    public static void setClosestPlayerTarget() {
        List<Zombie> zombies = (List<Zombie>) Bukkit.getWorld("world").getEntitiesByClass(Zombie.class);
        for (Zombie zombie : zombies) {
            List<Player> players = Bukkit.getWorld("world").getPlayers();
            players.sort(Comparator.comparingDouble(player -> player.getLocation().distance(zombie.getLocation())));
            if (!players.isEmpty()) {
                zombie.setTarget(players.get(0));
            }
        }
    }
}
