package ink.ziip.hammer.battlebox.listener;

import ink.ziip.hammer.battlebox.api.listener.BaseListener;
import ink.ziip.hammer.battlebox.api.object.team.TeamCard;
import ink.ziip.hammer.battlebox.api.object.user.User;
import ink.ziip.hammer.battlebox.manager.ConfigManager;
import ink.ziip.hammer.teams.api.object.Team;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OtherListener extends BaseListener {

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        User user = User.getUser(event.getPlayer());
        Team team = Team.getTeamByPlayer(event.getPlayer());
        if (event.getPlayer().getLocation().getY() < -64) {
            if (team == null) {
                user.teleport(ConfigManager.spawnLocation);
            } else {
                if (TeamCard.getTeamCard(team.getName()).getArea() == null) {
                    user.teleport(ConfigManager.spawnLocation);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        User.removeUser(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user = User.getUser(event.getPlayer());
        Team team = Team.getTeamByPlayer(event.getPlayer());
        if (team == null) {
            if (!user.getPlayer().hasPermission("battlebox.admin")) {
                user.teleport(ConfigManager.spawnLocation);
            }
        } else {
            if (TeamCard.getTeamCard(team.getName()).getArea() == null) {
                user.teleport(ConfigManager.spawnLocation);
            }
        }
    }
}
