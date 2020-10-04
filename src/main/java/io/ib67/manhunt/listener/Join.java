package io.ib67.manhunt.listener;

import io.ib67.manhunt.gui.vote.VoteGui;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join extends Base implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!getMh().gameStarted) {
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            e.getPlayer().getEquipment().clear();
            e.getPlayer().sendMessage(new String[]{
                "Hi There!This is ManHunt Minigame server.",
                "Game Introduction:",
                "In this game,3 hunters are designed to track and kill one runner.",
                "If runner die,hunters win,if someone kill the ender dragon,the runner win.",
                "Every hunter have a compass to track runner,and runner can be also known for hunter around him.",
                "But before getting compass,hunters should make a compass first."
        });
        }
        if (getMh().inGamePlayers.size() >= getMh().maxPlayers && !getMh().inGamePlayers.contains(e.getPlayer().getName())) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().sendMessage("Welcome back! Match already started! Please stay as a SPECTATOR and keep quiet.");
            return;
        }
        if (!getMh().inGamePlayers.contains(e.getPlayer().getName()) && getMh().voteGui == null) {
            getMh().inGamePlayers.add(e.getPlayer().getName());
        }
        if (Bukkit.getServer().getOnlinePlayers().size() == getMh().maxPlayers && getMh().voteGui == null && !getMh().gameStarted) {
            Bukkit.broadcastMessage("Start Vote!If you close gui in mistake,please reconnect to the server or use /vote.");
            getMh().voteGui = new VoteGui();
            Bukkit.getOnlinePlayers().forEach(p -> p.openInventory(getMh().voteGui.getInventory()));
        } else if (Bukkit.getServer().getOnlinePlayers().size() < getMh().maxPlayers) {
            Bukkit.broadcastMessage("Waiting for more player!! (" + Bukkit.getServer().getOnlinePlayers().size() + "/" + getMh().maxPlayers + ")");
        }
        if (getMh().inGamePlayers.contains(e.getPlayer().getName()) && !getMh().voted.contains(e.getPlayer().getUniqueId()) && getMh().voteGui != null) {
            e.getPlayer().openInventory(getMh().voteGui.getInventory());
        }

    }
}
