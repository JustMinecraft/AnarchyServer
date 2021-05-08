package net.justminecraft.anarchyserver;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "There are no rules here! Good luck!");
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
            e.getPlayer().setExp(0);
            e.getPlayer().setLevel(0);
            
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
            if (player != location && player.getGameMode() == GameMode.SURVIVAL) {
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

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (isBlockInSpawn(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockBurnEvent event) {
        if (isBlockInSpawn(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockExplodeEvent event) {
        if (isBlockInSpawn(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::isBlockInSpawn);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isBlockInSpawn);
    }

    @EventHandler
    public void onBlockFlow(BlockFromToEvent event) {
        if (isBlockInSpawn(event.getToBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean isBlockInSpawn(Block block) {
        return block.getWorld() == Bukkit.getWorlds().get(0)
                && distance(block.getLocation(), block.getWorld().getSpawnLocation()) <= Bukkit.getSpawnRadius();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION && event.getEntity() instanceof Vehicle) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getEntity().getWorld() == Bukkit.getWorlds().get(0)) {
            if (distance(event.getEntity().getLocation(), event.getEntity().getWorld().getSpawnLocation()) <= Bukkit.getSpawnRadius()) {
                event.setCancelled(true);
            } else if (distance(event.getEntity().getLocation(), event.getEntity().getWorld().getSpawnLocation()) <= Bukkit.getSpawnRadius() * 2
                    && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamageByEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getEntity().getWorld() == Bukkit.getWorlds().get(0)) {
            Entity damager = event.getDamager();

            while (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
                damager = (Entity) ((Projectile) damager).getShooter();
            }

            while (damager instanceof Tameable && ((Tameable) damager).getOwner() instanceof Entity) {
                damager = (Entity) ((Tameable) damager).getOwner();
            }

            if (damager instanceof Player && distance(damager.getLocation(), event.getEntity().getWorld().getSpawnLocation()) <= Bukkit.getSpawnRadius()) {
                event.setCancelled(true);
            }
        }
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
                "They have kindly donated " + halfPoints + " points to " + killer.getName() + "!"
        );
        
        Discord.send(discordMessage);
        
        Bukkit.getScheduler().runTaskLater(anarchyServer, () -> {
            event.getEntity().kickPlayer(event.getDeathMessage() + "\nYou are banned for 24 hours!");
            Bukkit.getBanList(BanList.Type.NAME).addBan(event.getEntity().getName(), event.getDeathMessage(), new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000), null);
        }, 100);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isBlockInSpawn(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isBlockInSpawn(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
