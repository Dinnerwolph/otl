package fr.dinnerwolph.otl.bungee.listener;

import fr.dinnerwolph.otl.bungee.BungeeOTL;
import fr.dinnerwolph.otl.bungee.listener.player.Join;
import fr.dinnerwolph.otl.bungee.listener.player.Quit;
import net.md_5.bungee.api.plugin.PluginManager;

/**
 * @author Dinnerwolph
 */

public class ListenerManager {

    public ListenerManager() {
        init();
    }

    public void init() {
        BungeeOTL instance = BungeeOTL.getInstance();
        PluginManager pm = instance.getProxy().getPluginManager();
        pm.registerListener(instance, new Join());
        pm.registerListener(instance, new Quit());
    }
}
