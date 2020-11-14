package io.ib67.manhunt.game;


import lombok.Builder;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * 为了Additions 可能要求扩展数据做的准备。
 */
@Builder
public class GamePlayer {
    @Getter
    private final Player player;
    @Getter
    private final Role role;

    public enum Role {
        RUNNER, HUNTER, SPECTATOR
    }
}
