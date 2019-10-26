package fr.dinnerwolph.otl.bukkit.listener;

import fr.dinnerwolph.otl.bukkit.BukkitOTL;
import fr.dinnerwolph.otl.bukkit.listener.player.Join;
import fr.dinnerwolph.otl.bukkit.listener.player.Quit;
import org.bukkit.Bukkit;

/**
 * @author Dinnerwolph
 */

public class ListenerManager {

    public ListenerManager() {
        init();
    }

    private void init() {
        Bukkit.getServer().getPluginManager().registerEvents(new Join(), BukkitOTL.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new Quit(), BukkitOTL.getInstance());
    }
}
