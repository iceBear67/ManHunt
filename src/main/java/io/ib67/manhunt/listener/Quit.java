package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Quit extends Base implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!getMh().gameStarted && getMh().voteGui==null) getMh().inGamePlayers.remove(e.getPlayer().getName());
    }
}
