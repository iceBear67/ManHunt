package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftItem extends Base implements Listener {
    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getRecipe().getResult().getType() == Material.COMPASS && !getMh().autoCompass) {
            getMh().autoCompass = true;
            Bukkit.broadcastMessage(e.getWhoClicked().getName() + " unlocked unlimited compass.");
            Bukkit.getOnlinePlayers().stream().filter(a -> a.getGameMode() == GameMode.SURVIVAL).filter(b -> !b.getUniqueId().equals(getMh().runner)).forEach(c -> c.getInventory().addItem(new ItemStack(Material.COMPASS)));
        }
    }

}
