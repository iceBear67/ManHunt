package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Damage extends Base implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!getMh().gameStarted && e.getEntity().getType() == EntityType.PLAYER) {
            e.setCancelled(true);
            return;
        }
    }
}
