package fr.dinnerwolph.otl.bungee.server;

import fr.dinnerwolph.otl.bungee.BungeeOTL;
import io.netty.channel.Channel;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Dinnerwolph
 */

public class Server {

    private ServerInfo serverInfo;
    private final String name;
    private final Group group;
    private final int port;
    private final String base;
    private final int maxOnline;
    private String status = "INIT";
    private Channel channelHandler;
    private int numberOfPlayer;


    public Server(String name, Group group, int port, String base, int maxOnline) {
        this.name = name;
        this.group = group;
        this.port = port;
        this.base = base;
        this.maxOnline = maxOnline;
    }

    public void setChannel(Channel channel) {
        channelHandler = channel;
    }

    public Channel getChannel() {
        return channelHandler;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void registerIntoBungee(InetAddress address) {
        this.serverInfo = BungeeOTL.getInstance().getProxy().constructServerInfo(name, new InetSocketAddress(address, port), name, false);
        BungeeOTL.getInstance().getProxy().getServers().put(name, serverInfo);
    }

    public void deletefromBungee() {
        BungeeOTL.getInstance().getProxy().getServers().remove(name);
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public String getBase() {
        return base;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public int getNumberOfPlayer() {
        return numberOfPlayer;
    }

    public void setNumberOfPlayer(int numberOfPlayer) {
        this.numberOfPlayer = numberOfPlayer;
    }

    public int getMaxOnline() {
        return maxOnline;
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "Server{" +
                "serverInfo=" + serverInfo +
                ", name='" + name + '\'' +
                ", group=" + group +
                ", port=" + port +
                ", base='" + base + '\'' +
                ", maxOnline=" + maxOnline +
                ", status='" + status + '\'' +
                ", channelHandler=" + channelHandler +
                ", numberOfPlayer=" + numberOfPlayer +
                '}';
    }
}
