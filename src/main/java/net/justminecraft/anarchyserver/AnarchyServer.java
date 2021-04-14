package net.justminecraft.anarchyserver;

import net.justminecraft.survivalranks.SurvivalRanks;
import org.bukkit.Bukkit;
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
    }
}