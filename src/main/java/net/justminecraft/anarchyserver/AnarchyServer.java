package net.justminecraft.anarchyserver;

import net.justminecraft.survivalranks.SurvivalRanks;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AnarchyServer extends JavaPlugin {

    private static String discordWebhook;
    
    public static AnarchyServer getAnarchyServer() {
        return anarchyServer;
    }

    public static SurvivalRanks getSurvivalRanks() {
        return (SurvivalRanks) Bukkit.getPluginManager().getPlugin("SurvivalRanks");
    }

    private static AnarchyServer anarchyServer;

    public static String getDiscordWebhook() {
        return discordWebhook;
    }

    @Override
    public void onEnable() {
        anarchyServer = this;
        
        saveDefaultConfig();

        discordWebhook = getConfig().getString("discord-webhook");
        
        new AnarchyListener(this);
        
        try {
            Class.forName("puregero.network.VoteEvent");
            Bukkit.getPluginManager().registerEvents(new VoteListener(), this);
        } catch (ClassNotFoundException e) {
            getLogger().info("VoteEvent does not exist, VoteListener will not be registered");
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasAchievement(Achievement.OPEN_INVENTORY)) {
                    player.awardAchievement(Achievement.OPEN_INVENTORY);
                }
            }
        }, 100, 100);
    }
}