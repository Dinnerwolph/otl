package fr.dinnerwolph.otl.bungee.listener.player;

import fr.dinnerwolph.otl.bungee.BungeeOTL;
import fr.dinnerwolph.otl.bungee.netty.ServerHandler;
import fr.dinnerwolph.otl.bungee.server.Server;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dinnerwolph
 */

public class Quit implements Listener {

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        Collection<ProxiedPlayer> proxiedPlayers = BungeeOTL.getInstance().getProxy().getPlayers();
        List<Server> serverList = new ArrayList<>();
        for (String name : BungeeOTL.getInstance().serverMap.keySet()) {
            if (name.contains("Hub")) {
                serverList.add(BungeeOTL.getInstance().serverMap.get(name));
            }
        }
        int a = serverList.size() * BungeeOTL.getInstance().hubstart;
        if (a - proxiedPlayers.size() >= BungeeOTL.getInstance().hubstart + 1) {
            ServerHandler.stopHub(serverList);
        }
    }
}
