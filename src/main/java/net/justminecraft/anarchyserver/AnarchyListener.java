package net.justminecraft.anarchyserver;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = null;
        
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Entity killerEntity = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
            
            if (killerEntity instanceof Player) {
                killer = (Player) killerEntity;
            }
        }
        
        if (killer == null) {
            
        }
        
        event.getEntity().kickPlayer(event.getDeathMessage() + "\nYou are banned for 24 hours!");
        Bukkit.getBanList(BanList.Type.NAME).addBan(event.getEntity().getName(), event.getDeathMessage(), new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000), null);
    }
}
