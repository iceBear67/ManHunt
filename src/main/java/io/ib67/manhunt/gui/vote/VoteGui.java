package io.ib67.manhunt.gui.vote;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class VoteGui implements InventoryHolder {
    private Inventory inv;
    private Map<UUID, Integer> votes = new HashMap<>();

    public VoteGui() {
        inv = Bukkit.createInventory(this, 6 * 9, "Voting / Who will be runner?");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getGameMode() == GameMode.SPECTATOR) continue;
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwner(onlinePlayer.getName());
            skullMeta.setDisplayName(onlinePlayer.getName());
            skull.setItemMeta(skullMeta);
            inv.addItem(skull);
        }
    }

    public void vote(Player p) {
        votes.put(p.getUniqueId(), votes.getOrDefault(p.getUniqueId(), 0) + 1);
    }

    public UUID getRunner() {
        return votes.entrySet().stream().reduce((a, b) -> a.getValue() > b.getValue() ? a : b).getKey();
        /*AtomicInteger max = new AtomicInteger();
        AtomicReference<UUID> key = new AtomicReference<>();
        votes.forEach((k, v) -> {
            if (v > max.get()) {
                max.set(v);
                key.set(k);
            }
        });
        return key.get();*/
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
