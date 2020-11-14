package io.ib67.manhunt.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Consumer;

import io.ib67.manhunt.ManHunt;

public class Vote implements Listener, InventoryHolder {
    private LinkedList<UUID> shouldVote;
    private LinkedList<UUID> voted = new LinkedList<>();
    private TreeMap<UUID, Integer> voteMap = new TreeMap<>();
    private Inventory voteInv;
    private Consumer<Vote> callback;

    public Vote(List<UUID> shouldVote, Consumer<Vote> callback) {
        this.shouldVote = new LinkedList(shouldVote);
        this.callback = callback;
        this.voteInv = Bukkit.createInventory(this, (this.shouldVote.size() / 9 + (this.shouldVote.size() % 9 == 0 ? 0 : 1)) * 9);
    }

    public Vote(Stream<UUID> shouldVote, Consumer<Vote> callback) {
        this.shouldVote = shouldVote.collect(Collectors.toCollection(LinkedList::new));
        this.callback = callback;
        this.voteInv = Bukkit.createInventory(this, (this.shouldVote.size() / 9 + (this.shouldVote.size() % 9 == 0 ? 0 : 1)) * 9);
    }

    public Inventory getInventory() {
        return voteInv;
    }

    public void startVote() {
        Bukkit.getPluginManager().registerEvents(this, ManHunt.get());
        shouldVote.stream().map(Bukkit::getPlayer).forEach(p -> p.openInventory(voteInv));
    }

    public void endVote() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    public void vote(UUID from, UUID to) {
        if (!shouldVote.contains(from) || voted.contains(from))
            return;

        voteMap.put(to, voteMap.containsKey(to) ? voteMap.get(to) + 1 : 1);

        voted.add(from);

        if (allVoted()) {
            callback.accept(this);
            endVote();
        }
    }

    private boolean allVoted() {
        return voted.size() == shouldVote.size();
    }

    public Player getResult() {
        return Bukkit.getPlayer(voteMap.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).orElseThrow(() -> new IllegalStateException("Impossible null")).getKey());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player && event.getInventory().getHolder() instanceof Vote))
            return;
        Player p = (Player) event.getWhoClicked();
        ItemStack itemClicked = event.getCurrentItem();
        event.setCancelled(true);
        if (itemClicked.getType() != Material.PLAYER_HEAD)
            return;

        SkullMeta meta = (SkullMeta) itemClicked.getItemMeta();
        vote(p.getUniqueId(), meta.getOwningPlayer().getUniqueId());
    }
}
