package net.justminecraft.anarchyserver;

import net.justminecraft.survivalranks.SurvivalRanks;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AnarchyServer extends JavaPlugin {

    @Override
    public void onEnable() {
        new AnarchyListener(this);
    }
    
    public SurvivalRanks getSurvivalRanks() {
        return (SurvivalRanks) Bukkit.getPluginManager().getPlugin("SurvivalRanks");
    }
}