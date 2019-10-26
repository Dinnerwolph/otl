package fr.dinnerwolph.otl.bukkit.listener.player;

import fr.dinnerwolph.otl.bukkit.netty.ClientHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Dinnerwolph
 */

public class Quit implements Listener {

    @EventHandler
    public void onPLayerJoin(PlayerQuitEvent event) {
        ClientHandler.sendPlayerNumber(Bukkit.getOnlinePlayers().size() - 1);
    }
}
