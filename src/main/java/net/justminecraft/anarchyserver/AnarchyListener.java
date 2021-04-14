package net.justminecraft.anarchyserver;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Date;

public class AnarchyListener implements Listener {

    private final AnarchyServer anarchyServer;

    public AnarchyListener(AnarchyServer anarchyServer) {
        this.anarchyServer = anarchyServer;
        anarchyServer.getServer().getPluginManager().registerEvents(this, anarchyServer);
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPlayedBefore()) {
            Bukkit.getScheduler().runTask(anarchyServer, () -> {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Welcome " + e.getPlayer().getName() + " to JustAnarchy!");
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "This is hardcore - if you die you'll be banned for 24 hours!");
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "The only rule on this server is no hacking! Good luck!");
            });
            e.getPlayer().teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }
        
        if (e.getPlayer().isDead()) {
            e.getPlayer().setHealth(20);
            e.getPlayer().setSaturation(20);
            e.getPlayer().setFoodLevel(20);
            e.getPlayer().getActivePotionEffects().forEach(potionEffect -> e.getPlayer().removePotionEffect(potionEffect.getType()));
            e.getPlayer().getInventory().clear();
            e.getPlayer().getEquipment().clear();
            
            if (e.getPlayer().getBedSpawnLocation() != null) {
                e.getPlayer().teleport(e.getPlayer().getBedSpawnLocation());
            } else {
                e.getPlayer().teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
        }
    }
    
    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.getReason().equals("You have died. Game over, man, it's game over!")) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(event.getPlayer().getName());
            event.setCancelled(true);
        }
    }
    
    private Player getClosestPlayer(Player location) {
        double closestDistance = 128;
        Player closest = null;
        
        for (Player player : location.getWorld().getPlayers()) {
            if (player != location) {
                double distance = distance(player.getLocation(), location.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = player;
                }
            }
        }
        
        return closest;
    }

    private double distance(Location a, Location b) {
        return Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getZ() - b.getZ()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = null;
        
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Entity killerEntity = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
            
            while (killerEntity instanceof Projectile && ((Projectile) killerEntity).getShooter() instanceof Entity) {
                killerEntity = (Entity) ((Projectile) killerEntity).getShooter();
            }
            
            while (killerEntity instanceof Tameable && ((Tameable) killerEntity).getOwner() instanceof Entity) {
                killerEntity = (Entity) ((Tameable) killerEntity).getOwner();
            }
            
            if (killerEntity instanceof Player) {
                killer = (Player) killerEntity;
            }
        }
        
        if (killer == null) {
            killer = getClosestPlayer(event.getEntity());
        }
        
        int halfPoints = AnarchyServer.getSurvivalRanks().getPoints(event.getEntity()).getPoints() / 2;
        AnarchyServer.getSurvivalRanks().getPoints(event.getEntity()).scalePoints(0.5);
        AnarchyServer.getSurvivalRanks().updateRank(event.getEntity());
        
        if (killer != null) {
            AnarchyServer.getSurvivalRanks().getPoints(killer).incrementPoints(halfPoints);
            Player k = killer;
            Bukkit.getScheduler().runTask(anarchyServer, () -> k.sendMessage(ChatColor.GREEN + " + " + halfPoints + " points"));
        }
        
        String discordMessage = event.getDeathMessage() + "\n" + (
                killer == null ?
                "They have lost " + halfPoints + " points!" :
                "They have kindly doanted " + halfPoints + " points to " + killer.getName() + "!"
        );
        
        Discord.send(discordMessage);
        
        event.getEntity().kickPlayer(event.getDeathMessage() + "\nYou are banned for 24 hours!");
        Bukkit.getBanList(BanList.Type.NAME).addBan(event.getEntity().getName(), event.getDeathMessage(), new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000), null);
    }
}
