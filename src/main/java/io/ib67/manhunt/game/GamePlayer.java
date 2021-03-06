package io.ib67.manhunt.game;


import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Objects;

/**
 * 为了Additions 可能要求扩展数据做的准备。
 */
@Builder
public class GamePlayer {
    private final String player;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Role role;

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public enum Role {
        RUNNER, HUNTER
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof GamePlayer && Objects.equals(this.player, ((GamePlayer) obj).player));
    }
}
