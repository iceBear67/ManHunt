package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Move extends Base implements Listener {
    private Set<String> nearbyPlayers = new HashSet<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!getMh().gameStarted) {
            if (e.getTo().distance(getMh().mainWorld.getSpawnLocation()) > 30) {
                e.getPlayer().teleport(getMh().mainWorld.getSpawnLocation());
            }
        } else {
            if (e.getPlayer().getUniqueId().equals(getMh().runner)) {
                getMh().lastLoc.put(e.getPlayer().getLocation().getWorld().getUID(), e.getPlayer().getLocation());
                if (e.getTo().getWorld().getEnvironment() == World.Environment.NETHER) {
                    if (!getMh().gotoNether) {
                        Bukkit.broadcastMessage("[*] Runner arrived nether.");
                        getMh().gotoNether = true;
                    }
                } else if (e.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
                    if (!getMh().gotoEnd) {
                        Bukkit.broadcastMessage("[*] Runner arrived END");
                        getMh().gotoEnd = true;
                    }
                }
            } else if (e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                Player runner = Bukkit.getPlayer(getMh().runner);
                if (runner == null) {
                    return;
                }
                if (e.getPlayer().getWorld() != runner.getWorld()) {
                    return;
                }
                double distance = e.getPlayer().getLocation().distance(runner.getLocation());
                if (distance < 30 && !nearbyPlayers.contains(e.getPlayer().getName())) {
                    TextComponent msg = new TextComponent(e.getPlayer().getName() + " is near you. (total " + nearbyPlayers.size() + " hunters,in 30m)");
                    msg.setColor(ChatColor.AQUA);
                    if (distance < 10) {
                        msg.addExtra(" | DISTANCE: " + distance);
                        msg.setColor(ChatColor.RED);
                    }
                    runner.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg);
                    nearbyPlayers.add(e.getPlayer().getName());
                } else if (distance >= 30 && nearbyPlayers.contains(e.getPlayer().getName())) {
                    nearbyPlayers.remove(e.getPlayer().getName());
                    TextComponent msg = new TextComponent(e.getPlayer().getName() + " isn't near you now. (total " + nearbyPlayers.size() + " hunters,in 30m)");
                    msg.setColor(ChatColor.AQUA);
                    runner.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
                if (nearbyPlayers.size() == 0) {
                    TextComponent msg = new TextComponent("Safe.No hunters in 30m.");
                    msg.setColor(ChatColor.GREEN);
                    runner.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }
    }
}
