package io.ib67.manhunt.game;

import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Game implements Listener {
    protected List<GamePlayer> inGamePlayers = new LinkedList<>();
    private final int playersToStart;
    private Supplier<Game> gameEnd;
    private Supplier<Game> gameStart;

    public Game(int playersToStart, Supplier<Game> gameStart, Supplier<Game> gameEnd) {
        this.gameStart = gameStart;
        this.gameEnd = gameEnd;
        this.playersToStart = playersToStart;
    }

    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            //Player Died Event
        }
    }

    public boolean isInGame(String name) {
        return inGamePlayers.stream().anyMatch(s -> s.getPlayer().getName().equals(name));
    }
}
