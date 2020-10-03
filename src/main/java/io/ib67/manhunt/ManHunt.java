package io.ib67.manhunt;

import io.ib67.manhunt.gui.vote.VoteGui;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class ManHunt extends JavaPlugin implements Listener {
    private UUID runner;
    private int maxPlayers;
    private List<UUID> inGamePlayers = new ArrayList<>();
    private boolean gotoEnd = false;
    private boolean gotoNether = false;
    private World mainWorld;
    private boolean gameStarted = false;
    private Map<UUID, Location> lastLoc = new HashMap<>();

    private int votedPlayers = 0;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getDataFolder().mkdir();
        saveDefaultConfig();
        mainWorld = getServer().getWorld((String) getConfig().get("mainWorld"));
        mainWorld.setDifficulty(Difficulty.PEACEFUL);
        mainWorld.setTime(100);
        mainWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getServer().getPluginManager().registerEvents(this, this);
        maxPlayers = getConfig().getInt("players");
        getLogger().info("World init completed.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void stopGame(GameResult result) {
        gameStarted = false;
        getServer().getOnlinePlayers().forEach(e -> e.setGameMode(GameMode.SPECTATOR));
        if (result == GameResult.RUNNER_WIN) {
            getServer().getOnlinePlayers().forEach(e -> e.sendTitle("RUNNER WON!", "Don't give up!Try next time."));
        } else {
            getServer().getOnlinePlayers().forEach(e -> e.sendTitle("HUNTER WON!", "Don't give up!Try next time."));
        }
        Location nextSpawnLoc = genRandomSpawnLoc();
        getConfig().set("nextSpawnLoc", nextSpawnLoc);
        mainWorld.setSpawnLocation(nextSpawnLoc);
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            onlinePlayer.getInventory().clear();
            onlinePlayer.setBedSpawnLocation(mainWorld.getSpawnLocation());
            onlinePlayer.teleport(mainWorld.getSpawnLocation());
        }
        Bukkit.broadcastMessage("Server is going to restart in 5s for next game.");
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getServer().shutdown();
        }, 5 * 20L);
    }

    public Location genRandomSpawnLoc() {
        Random random = new Random();
        Location loc = mainWorld.getHighestBlockAt(random.nextInt(100000), random.nextInt(100000)).getLocation().clone();
        loc.setY(loc.getY() + 1);
        return loc;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!gameStarted && e.getEntity().getType() == EntityType.PLAYER) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!gameStarted) {
            if (e.getTo().distance(mainWorld.getSpawnLocation()) > 30) {
                e.getPlayer().teleport(mainWorld.getSpawnLocation());
            }
        } else {
            if (e.getPlayer().getUniqueId().equals(runner)) {
                lastLoc.put(e.getPlayer().getLocation().getWorld().getUID(), e.getPlayer().getLocation());
                if (e.getTo().getWorld().getEnvironment() == World.Environment.NETHER) {
                    if (!gotoNether) {
                        Bukkit.broadcastMessage("[*] Runner arrived nether.");
                        gotoNether = true;
                    }
                } else if (e.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
                    if (!gotoEnd) {
                        Bukkit.broadcastMessage("[*] Runner arrived END");
                        gotoEnd = true;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            if (e.getEntity().getUniqueId().equals(runner)) {
                stopGame(GameResult.HUNTER_WIN);
            }
        } else if (e.getEntity().getType() == EntityType.ENDER_DRAGON) {
            stopGame(GameResult.RUNNER_WIN);
        }
    }

    public ItemStack refreshCompass(Location loc) {
        NBTUtil.NBTValue x = new NBTUtil.NBTValue().set(loc.getBlockX());
        NBTUtil.NBTValue y = new NBTUtil.NBTValue().set(loc.getBlockY());
        NBTUtil.NBTValue z = new NBTUtil.NBTValue().set(loc.getBlockZ());
        Object compound = NBTUtil.setTagValue(NBTUtil.newNBTTagCompound(), "x", x);
        compound = NBTUtil.setTagValue(compound, "y", y);
        compound = NBTUtil.setTagValue(compound, "z", z);
        ItemStack modified = NBTUtil.setTagValue(new ItemStack(Material.COMPASS), "LodestoneTracked", new NBTUtil.NBTValue().set(false));
        modified = NBTUtil.setTagValue(modified, "LodestonePos", new NBTUtil.NBTValue(compound));
        modified = NBTUtil.setTagValue(modified,"LodestoneDimension",new NBTUtil.NBTValue().set(envAsName(loc.getWorld().getEnvironment())));
        return modified;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!gameStarted) {
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            e.getPlayer().getEquipment().clear();
        }
        if (getServer().getOnlinePlayers().size() == maxPlayers) {
            inGamePlayers.add(e.getPlayer().getUniqueId());
            VoteGui voteGui = new VoteGui();
            Bukkit.getOnlinePlayers().forEach(p -> p.openInventory(voteGui.getInventory()));
        } else {
            inGamePlayers.add(e.getPlayer().getUniqueId());
            Bukkit.broadcastMessage("Waiting for more player!! (" + getServer().getOnlinePlayers().size() + "/" + maxPlayers + ")");
        }
        if (gameStarted && !inGamePlayers.contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().sendMessage("Welcome back! Match already started! Please stay as a SPECTATOR and keep quiet.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!gameStarted) inGamePlayers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            e.setFormat(ChatColor.GRAY + "[ SPECTATOR ] %1$s: %2$s");
            return;
        }
        if (gameStarted) {
            if (runner.equals(e.getPlayer().getUniqueId())) {
                e.setFormat(ChatColor.GREEN + "[ RUNNER ] %1$s:" + ChatColor.WHITE + " %2$s");
            } else {
                e.setFormat(ChatColor.RED + "[ HUNTER ] %1$s:" + ChatColor.WHITE + " %2$s");
            }
        } else {
            e.setFormat(ChatColor.GRAY + "%1$s:" + ChatColor.WHITE + " %2$s");
        }
        if (e.getMessage().startsWith("#") && !e.getPlayer().getUniqueId().equals(runner)) {
            e.setCancelled(true);
            getServer().getOnlinePlayers()
                    .stream()
                    .filter(p -> !p.getUniqueId().equals(runner))
                    .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                    .forEach(s -> s.sendMessage("[ TEAM ] " + String.format(e.getFormat(), e.getPlayer().getName(), e.getMessage())));
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory().getHolder() == null) return;
        if (e.getClickedInventory().getHolder() instanceof VoteGui) {
            e.setResult(Event.Result.DENY);
            e.setCancelled(true);
            VoteGui voteGui = (VoteGui) e.getClickedInventory().getHolder();
            if (e.getCurrentItem().getType() != Material.PLAYER_HEAD) {
                e.setResult(Event.Result.DENY);
                e.setCancelled(true);
            } else {
                voteGui.vote(Objects.requireNonNull(Bukkit.getPlayerExact(e.getCurrentItem().getItemMeta().getDisplayName())));
                votedPlayers++;
                Bukkit.broadcastMessage("Voting.. " + votedPlayers + "/" + inGamePlayers.size());
                if (votedPlayers == inGamePlayers.size()) {
                    startGame(voteGui.getRunner());
                }
                e.setResult(Event.Result.DENY);
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR && e.getItem().getType() == Material.COMPASS && gameStarted) {
            Player theRunner = Bukkit.getPlayer(runner);
            Location loc = lastLoc.get(e.getPlayer().getWorld().getUID());
            if (loc == null) {
                e.getPlayer().sendMessage("Maybe Runner haven't arrived this world yet.");
                return;
            }
            if (!theRunner.getLocation().getWorld().getName().equals(e.getPlayer().getLocation().getWorld().getName())) {
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("WARNING: THE RUNNER ISN'T IN THE SAME WORLD!", net.md_5.bungee.api.ChatColor.RED));
            } else {
                TextComponent message = new TextComponent("TRACKING: " + theRunner.getName());
                message.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                TextComponent distance = new TextComponent(" DISTANCE: " + loc.distance(e.getPlayer().getLocation()));
                distance.setColor(net.md_5.bungee.api.ChatColor.RED);
                message.addExtra(distance);
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
            }
            e.getPlayer().getEquipment().setItemInMainHand(refreshCompass(loc));
        }
    }

    private void startGame(UUID runner) {
        gameStarted = true;
        this.runner = runner;
        getServer().getOnlinePlayers().forEach(e -> e.setGameMode(GameMode.SURVIVAL));

        mainWorld.setDifficulty(Difficulty.HARD);
        mainWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        Bukkit.broadcastMessage(ChatColor.RED + "GAME STARTED!! RUNNER: " + Bukkit.getPlayer(runner).getName());
        Bukkit.broadcastMessage(ChatColor.GREEN + "For the hunters, add a prefix " + ChatColor.BOLD + "#" + ChatColor.RESET + ChatColor.GREEN + " for team speaking.");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.getUniqueId().equals(runner)) {
                onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, getConfig().getInt("hunterWaitTime") * 20, 4));
                onlinePlayer.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
        }
        Bukkit.getPlayer(runner).sendTitle("Run!", "HUNTERS CAN'T MOVE FOR " + getConfig().getInt("hunterWaitTime") + " SECONDS", 20, 40, 20);

    }
    private String envAsName(World.Environment env){
      switch(env){
        case NORMAL:
          return "minecraft:overworld";
        case THE_END:
          return "minecraft:the_end";
        case NETHER:
          return "minecraft:the_nether";
      }
      System.err.println("SOMETHING WRONG IN envAsName!! "+env);
      return "";
    }
    public UUID getRunner() {
        return runner;
    }
}
