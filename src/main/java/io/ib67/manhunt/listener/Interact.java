package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class Interact extends Base implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().getType() == Material.COMPASS && getMh().gameStarted) {
            Player theRunner = Bukkit.getPlayer(getMh().runner);
            if(theRunner==null){
              e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Runner offline!", net.md_5.bungee.api.ChatColor.RED));
              return;
            }
            Location loc = getMh().lastLoc.get(e.getPlayer().getWorld().getUID());
            if (loc == null) {
                e.getPlayer().sendMessage("Maybe Runner haven't arrived this world yet.");
                return;
            }
            if (!theRunner.getLocation().getWorld().getName().equals(e.getPlayer().getLocation().getWorld().getName())) {
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("WARNING: THE RUNNER ISN'T IN THE SAME WORLD!", net.md_5.bungee.api.ChatColor.RED));
            } else {
                TextComponent message = new TextComponent("TRACKING: " + theRunner.getName());
                message.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                if (loc.distance(e.getPlayer().getLocation()) > 200) {
                    TextComponent distance = new TextComponent(" DISTANCE > 200 ");
                    distance.setColor(net.md_5.bungee.api.ChatColor.RED);
                    message.addExtra(distance);
                    if (loc.distance(e.getPlayer().getLocation()) > 500) {
                        message.addExtra(new TextComponent("(" + loc.distance(e.getPlayer().getLocation()) + ")"));
                    }
                }

                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
            }
            e.getPlayer().getEquipment().setItemInMainHand(getMh().refreshCompass(loc));
        }
    }
}
