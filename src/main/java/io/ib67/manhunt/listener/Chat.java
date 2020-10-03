package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Chat extends Base implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            e.setFormat(ChatColor.GRAY + "[ SPECTATOR ] %1$s: %2$s");
            return;
        }
        if (getMh().gameStarted) {
            if (getMh().runner.equals(e.getPlayer().getUniqueId())) {
                e.setFormat(ChatColor.GREEN + "[ RUNNER ] %1$s:" + ChatColor.WHITE + " %2$s");
            } else {
                e.setFormat(ChatColor.RED + "[ HUNTER ] %1$s:" + ChatColor.WHITE + " %2$s");
            }
        } else {
            e.setFormat(ChatColor.GRAY + "%1$s:" + ChatColor.WHITE + " %2$s");
        }
        if (e.getMessage().startsWith("#") && !e.getPlayer().getUniqueId().equals(getMh().runner)) {
            e.setCancelled(true);
            getMh().getServer().getOnlinePlayers()
                    .stream()
                    .filter(p -> !p.getUniqueId().equals(getMh().runner))
                    .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                    .forEach(s -> s.sendMessage("[ TEAM ] " + String.format(e.getFormat(), e.getPlayer().getName(), e.getMessage().replaceFirst("#", ""))));
        }
    }
}
