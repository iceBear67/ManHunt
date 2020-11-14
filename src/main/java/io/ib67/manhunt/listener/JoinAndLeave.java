package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import io.ib67.manhunt.game.GamePhase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinAndLeave implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ManHunt.get().getGame().joinPlayer(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (ManHunt.get().getGame().getPhase() == GamePhase.WAITING_FOR_PLAYER) {
            ManHunt.get().getGame().kickPlayer(event.getPlayer().getName());
        }
    }
}
