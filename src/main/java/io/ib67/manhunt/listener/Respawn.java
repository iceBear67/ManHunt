package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class Respawn extends Base implements Listener {

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.getPlayer().getUniqueId().equals(getMh().runner) && getMh().autoCompass) {
            e.getPlayer().getEquipment().setItemInMainHand(new ItemStack(Material.COMPASS));
        }
    }
}
