package io.ib67.manhunt;

import io.ib67.manhunt.gui.vote.VoteGui;
import io.ib67.manhunt.listener.*;
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

public final class ManHunt extends JavaPlugin {
    public UUID runner;
    public int maxPlayers;
    public Set<String> inGamePlayers = new HashSet<>();
    public boolean gotoEnd = false;
    public boolean gotoNether = false;
    public World mainWorld;
    public boolean gameStarted = false;
    public Map<UUID, Location> lastLoc = new HashMap<>();

    public List<UUID> voted = new ArrayList<>();
    public VoteGui voteGui;
    public boolean autoCompass = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getDataFolder().mkdir();
        saveDefaultConfig();
        mainWorld = getServer().getWorld((String) getConfig().get("mainWorld"));
        mainWorld.setDifficulty(Difficulty.PEACEFUL);
        mainWorld.setTime(100);
        mainWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getServer().getPluginManager().registerEvents(new Chat(), this);
        getServer().getPluginManager().registerEvents(new CraftItem(), this);
        getServer().getPluginManager().registerEvents(new Damage(), this);
        getServer().getPluginManager().registerEvents(new Death(), this);
        getServer().getPluginManager().registerEvents(new Interact(), this);
        getServer().getPluginManager().registerEvents(new InvClick(), this);
        getServer().getPluginManager().registerEvents(new Join(), this);
        getServer().getPluginManager().registerEvents(new Move(), this);
        getServer().getPluginManager().registerEvents(new Quit(), this);
        getServer().getPluginManager().registerEvents(new Respawn(), this);
        maxPlayers = getConfig().getInt("players");
        getLogger().info("World init completed.");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You're not a player.");
            return true;
        }
        Player player = (Player) sender;
        if (label.equalsIgnoreCase("vote")) {
            if (!voted.contains(player.getUniqueId()) && voteGui != null) {
                player.openInventory(voteGui.getInventory());
            } else {
                player.sendMessage("Vote haven't started or you've voted yet!");
            }
        } else if (label.equalsIgnoreCase("hunt")) {
            if (args.length != 1) {
                return false;
            }
            Player target = Bukkit.getPlayer(UUID.fromString(args[0]));
            if (target == null) {
                sender.sendMessage("Failed to find player.");
                return true;
            }
            runner = target.getUniqueId();
            Bukkit.broadcastMessage("[*] Runner has been set to " + target.getName());
        }
        return true;
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
        Bukkit.broadcastMessage("Server is going to restart in 60s for next game.");
        Bukkit.getScheduler().runTaskLater(this, () -> getServer().shutdown(), 60 * 20L);
    }

    public Location genRandomSpawnLoc() {
        Random random = new Random();
        Location loc = mainWorld.getHighestBlockAt(random.nextInt(100000), random.nextInt(100000)).getLocation().clone();
        loc.setY(loc.getY() + 1);
        return loc;
    }



    public ItemStack refreshCompass(Location loc) {
        NBTUtil.NBTValue x = new NBTUtil.NBTValue().set(loc.getBlockX());
        NBTUtil.NBTValue y = new NBTUtil.NBTValue().set(loc.getBlockY());
        NBTUtil.NBTValue z = new NBTUtil.NBTValue().set(loc.getBlockZ());
        Object compound = NBTUtil.setTagValue(NBTUtil.newNBTTagCompound(), "X", x);
        compound = NBTUtil.setTagValue(compound, "Y", y);
        compound = NBTUtil.setTagValue(compound, "Z", z);
        ItemStack modified = NBTUtil.setTagValue(new ItemStack(Material.COMPASS), "LodestoneTracked", new NBTUtil.NBTValue().set(false));
        modified = NBTUtil.setTagValue(modified, "LodestonePos", new NBTUtil.NBTValue(compound));
        modified = NBTUtil.setTagValue(modified,"LodestoneDimension",new NBTUtil.NBTValue().set(envAsName(loc.getWorld().getEnvironment())));
        return modified;
    }

    public void startGame(UUID runner) {
        voteGui = null;
        gameStarted = true;
        this.runner = runner;
        getServer().getOnlinePlayers().forEach(e -> e.setGameMode(GameMode.SURVIVAL));

        mainWorld.setDifficulty(Difficulty.HARD);
        mainWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        Bukkit.broadcastMessage(ChatColor.RED + "GAME STARTED!! RUNNER: " + Bukkit.getPlayer(runner).getName());
        Bukkit.broadcastMessage(ChatColor.GREEN + "For the hunters, add a prefix " + ChatColor.WHITE + "#" + ChatColor.RESET + ChatColor.GREEN + " for team speaking.");
        Player target = getServer().getPlayer(runner);
        Location loc = target.getLocation().clone();
        loc.setX(loc.getX() + new Random().nextInt(1000) + 500);
        loc.setZ(loc.getZ() + new Random().nextInt(1000) + 500);
        loc.getWorld().getHighestBlockAt(loc).setType(Material.GLASS);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc));
        target.teleport(loc);
        target.sendMessage("You've teleported to a place which is very far from hunters.(" + loc.distance(mainWorld.getSpawnLocation()) + "m)");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.getUniqueId().equals(runner)) {
                onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, getConfig().getInt("hunterWaitTime") * 20, 10));
            }
        }
        Bukkit.getPlayer(runner).sendTitle("Start your speedrun!", "AND HUNTERS CAN'T MOVE FOR " + getConfig().getInt("hunterWaitTime") + " SECONDS", 20, 40, 20);

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
}
