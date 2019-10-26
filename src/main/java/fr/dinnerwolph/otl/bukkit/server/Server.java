package fr.dinnerwolph.otl.bukkit.server;

/**
 * @author Dinnerwolph
 */

public class Server {

    private final String serverName;
    private String status;
    private int onlineAmount;
    private int maxOnline;

    public Server(String serverName, int onlineAmount, String status, long maxOnline) {
        this.serverName = serverName;
        this.onlineAmount = onlineAmount;
        this.status = status;
        this.maxOnline = (int ) maxOnline;
    }

    public String getServerName() {
        return serverName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getOnlineAmount() {
        return onlineAmount;
    }

    public void setOnlineAmount(int onlineAmount) {
        this.onlineAmount = onlineAmount;
    }

    public int getMaxAmount() {
        return maxOnline;
    }

    public void setMaxOnline(int maxOnline) {
        this.maxOnline = maxOnline;
    }
}
