package fr.dinnerwolph.otl.bukkit.event;

import fr.dinnerwolph.otl.bukkit.server.Server;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Dinnerwolph
 */
public class ServerUpdateEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    private Server server;

    public ServerUpdateEvent(Server server) {
        this.server = server;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Server getServer() {
        return server;
    }
}
