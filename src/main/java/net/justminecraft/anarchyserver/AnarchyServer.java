package net.justminecraft.anarchyserver;

import org.bukkit.plugin.java.JavaPlugin;

public class AnarchyServer extends JavaPlugin {

    @Override
    public void onEnable() {
        new AnarchyListener(this);
    }
}