package nl.bergthaler.zombiesurvival;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.List;

public class zombieSurvival {
    public static void setClosestPlayerTarget() {
        List<Zombie> zombies = (List<Zombie>) Bukkit.getWorld("world").getEntitiesByClass(Zombie.class);
        for (Zombie zombie : zombies) {
            List<Player> players = Bukkit.getWorld("world").getPlayers();
            players.sort(Comparator.comparingDouble(player -> player.getLocation().distance(zombie.getLocation())));
            if (!players.isEmpty()) {
                Player player = players.get(0);
                Block block = player.getTargetBlock(null, 100);
                if (block != null && !block.getType().equals(Material.STONE_BRICKS)
                        && !block.getType().equals(Material.IRON_DOOR)
                        && !block.getType().equals(Material.TINTED_GLASS)) {
                    block.breakNaturally();
                }
                zombie.setTarget(player);
            }
        }
    }
}
