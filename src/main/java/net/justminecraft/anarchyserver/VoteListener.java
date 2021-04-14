package net.justminecraft.anarchyserver;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import puregero.network.VoteEvent;

public class VoteListener implements org.bukkit.event.Listener {
    @EventHandler
    public void onVote(VoteEvent e) {
        e.getPlayer().sendMessage(ChatColor.GREEN + "Thank you for voting for us at " + e.getWebsite() + ".");
        e.getPlayer().sendMessage(ChatColor.GREEN + "You have earnt some bonus points towards your next rank!");
        e.getPlayer().sendMessage(ChatColor.GREEN + " + 2500 points");
        AnarchyServer.getSurvivalRanks().getPoints(e.getPlayer()).incrementPoints(2500);
    }
}
