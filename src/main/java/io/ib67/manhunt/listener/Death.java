package io.ib67.manhunt.listener;

import io.ib67.manhunt.GameResult;
import io.ib67.manhunt.ManHunt;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class Death extends Base implements Listener {
    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            if (e.getEntity().getUniqueId().equals(getMh().runner)) {
                getMh().stopGame(GameResult.HUNTER_WIN);
            }
        } else if (e.getEntity().getType() == EntityType.ENDER_DRAGON) {
            getMh().stopGame(GameResult.RUNNER_WIN);
        }
    }
}
