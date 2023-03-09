package nl.bergthaler.zombiesurvival;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.List;
public class Zombiesurvival extends JavaPlugin {
    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Zombie> zombies = (List<Zombie>) Bukkit.getWorld("world").getEntitiesByClass(Zombie.class);
                for (Zombie zombie : zombies) {
                    List<Player> players = Bukkit.getWorld("world").getPlayers();
                    players.sort(Comparator.comparingDouble(player -> player.getLocation().distance(zombie.getLocation())));
                    if (!players.isEmpty()) {
                        zombie.setTarget(players.get(0));
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
