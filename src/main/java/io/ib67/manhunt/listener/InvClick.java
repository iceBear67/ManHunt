package io.ib67.manhunt.listener;

import io.ib67.manhunt.ManHunt;
import io.ib67.manhunt.gui.vote.VoteGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class InvClick extends Base implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || e.getClickedInventory().getHolder() == null) return;
        if (e.getClickedInventory().getHolder() instanceof VoteGui) {
            e.setResult(Event.Result.DENY);
            e.setCancelled(true);
            VoteGui voteGui = (VoteGui) e.getClickedInventory().getHolder();
            if (e.getCurrentItem().getType() != Material.PLAYER_HEAD) {
                e.setResult(Event.Result.DENY);
                e.setCancelled(true);
            } else {
                voteGui.vote(Objects.requireNonNull(Bukkit.getPlayerExact(e.getCurrentItem().getItemMeta().getDisplayName())));
                getMh().voted.add(e.getWhoClicked().getUniqueId());
                Bukkit.broadcastMessage("Voting.. " + getMh().voted.size() + "/" + getMh().inGamePlayers.size());
                if (getMh().voted.size() == getMh().inGamePlayers.size()) {
                    getMh().startGame(voteGui.getRunner());
                }
                e.setResult(Event.Result.DENY);
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
            }
        }
    }
}
