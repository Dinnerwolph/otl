package fr.dinnerwolph.otl.bungee.listener.player;

import fr.dinnerwolph.otl.bungee.BungeeOTL;
import fr.dinnerwolph.otl.bungee.netty.ServerHandler;
import fr.dinnerwolph.otl.bungee.server.Server;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dinnerwolph
 */

public class Join implements Listener {

    @EventHandler()
    public void onPlayerJoin(PreLoginEvent event) {
        if (BungeeOTL.getInstance().start) {
            event.registerIntent(BungeeOTL.getInstance());
            event.setCancelled(true);
            event.setCancelReason(new TextComponent("Serveur en cours de lancement."));
            event.getConnection().disconnect(new TextComponent("Serveur en cours de lancement."));
            event.completeIntent(BungeeOTL.getInstance());
            return;
        }
        /**Collection<ProxiedPlayer> proxiedPlayers = BungeeOTL.getInstance().getProxy().getPlayers();
        List<Server> serverList = new ArrayList<>();
        for (String name : BungeeOTL.getInstance().serverMap.keySet())
            if (name.contains("Hub"))
                serverList.add(BungeeOTL.getInstance().serverMap.get(name));
        int a = serverList.size() * BungeeOTL.getInstance().hubstart;
        /**if (a - proxiedPlayers.size() <= 10)
            ServerHandler.startNewHub(serverList.size() + 1);*/
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void a(ProxyPingEvent event) {
        if (BungeeOTL.getInstance().start) {
            event.getResponse().setVersion(new ServerPing.Protocol("Serveur en cours de lancement.", -1));
            event.getResponse().setDescriptionComponent(new TextComponent("Serveur en cours de lancement. (" + getPourcent() + "%)"));
        }
    }

    private String getPourcent() {
        if(BungeeOTL.getInstance().max == 0)
            return "0";
        return "" + BungeeOTL.getInstance().count * 100 / BungeeOTL.getInstance().max;
    }

}
