package fr.dinnerwolph.otl.bungee.netty;

import fr.dinnerwolph.otl.base.Base;
import fr.dinnerwolph.otl.bungee.BungeeOTL;
import fr.dinnerwolph.otl.bungee.server.Group;
import fr.dinnerwolph.otl.bungee.server.Server;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author Dinnerwolph
 */

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final BungeeOTL plugin;
    private static ServerHandler instance;
    private Map<Channel, Integer> a = new HashMap<>();
    private Map<Channel, String> b = new HashMap<>();
    private Map<Channel, String> c = new HashMap<>();
    private static boolean stop;
    private final int[] version = {47, 110, 340, 498};
    private final String[] serverList = {"Hub1-8", "Hub1-9", "Hub1-12", "Hub1-14"};

    public ServerHandler(BungeeOTL plugin) {
        this.plugin = plugin;
        instance = this;
        stop = false;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            b.put(ctx.channel(), c(ctx.channel()) + msg);
            a(ctx.channel());
        } catch (Exception e) {
            Base.error("Error while parsing JSON message: ");
            e.printStackTrace();
        }
    }


    private void a(Channel channel) {
        String[] var2 = c(channel).split("");
        for (String var5 : var2) {
            if (var5.equals("{"))
                a.put(channel, b(channel) + 1);

            if (b(channel) > 0) {
                c.put(channel, d(channel) + var5);
                b.put(channel, c(channel).substring(1));
            }

            if (var5.equals("}")) {
                a.put(channel, b(channel) - 1);
                if (b(channel) == 0) {
                    try {
                        a((JSONObject) JSONValue.parse(d(channel)), channel);
                    } catch (Exception e) {
                        BungeeOTL.error("Error while parsing JSON message: " + d(channel));
                        e.printStackTrace();
                    }

                    c.put(channel, "");
                }
            }
        }
    }

    private void a(JSONObject object, Channel channel) {
        if (plugin.debug)
            System.out.println(object.toJSONString());
        final String server = (String) object.get("server");
        final String type = (String) object.get("type");
        Object data = object.get("data");

        switch (type) {
            case "BASE_HANDSHAKE":
                if (plugin.channelMap.containsKey(server)) {
                    BungeeOTL.error("the server " + server + " is already registered");
                } else {
                    ChannelFuture future = channel.closeFuture();
                    future.addListener((futurel) -> {
                        for (String name : plugin.channelMap.keySet()) {
                            if (plugin.channelMap.get(name).closeFuture() == futurel) {
                                System.out.println(name + " has disconnected");
                                plugin.channelMap.remove(name);
                                plugin.serverMap.remove(name);
                            }
                        }
                    });

                    plugin.channelMap.put(server, channel);
                    for (Group group : plugin.groupList.values()) {
                        if (group.isBaseGroup(server) && group.isStartOnInit()) {
                            channel.writeAndFlush(format("SEND_GROUP", format(group.getGroupName(), group.getOnlineAmount(), group.getMaxAmount(), group.getRamInMegabyte(), group.getPluginsList())));
                            plugin.max += group.getOnlineAmount();
                        }
                    }
                }

                break;
            case "INIT_SERVER": {
                Group group = null;
                for (Group groups : plugin.groupList.values())
                    if (server.contains(groups.getGroupName()))
                        group = groups;
                long port = (Long) data;
                Server servers = new Server(server, group, (int) port, (String) object.get("base"), group.getMaxOnline());
                plugin.serverMap.put(server, servers);

                break;
            }
            case "SERVER_HANDSHAKE": {
                ChannelFuture future = channel.closeFuture();
                future.addListener((futurel) -> {
                    try {
                        for (String name : plugin.channelMap.keySet()) {
                            if (plugin.channelMap.get(name).closeFuture() == future) {
                                System.out.println(name + " has disconnected");
                                plugin.channelMap.remove(name);
                                plugin.serverMap.get(name).deletefromBungee();
                                Server temp = plugin.serverMap.get(name);
                                serverCloed(temp);
                                plugin.serverMap.remove(name);
                                //TODO à voir car ça pose problème au restartgroup a(name, temp);
                            }
                        }
                    } catch (Exception ignored) {

                    }
                });

                plugin.channelMap.put(server, channel);
                Server servers = plugin.serverMap.get(server);
                servers.setChannel(channel);
                servers.setStatus(String.valueOf(data));
                servers.registerIntoBungee(((InetSocketAddress) channel.remoteAddress()).getAddress());
                plugin.count++;
                if (plugin.count >= plugin.max)
                    plugin.start = false;
                for (String serverkey : plugin.serverMap.keySet())
                    sendInformation(plugin.serverMap.get(serverkey));

                break;
            }
            case "STOP_SERVER":
                if (stop && Integer.parseInt((String) data) == 0) {
                    stop = false;
                    channel.writeAndFlush(format("STOP_SERVER", "ACCEPT"));
                    plugin.channelMap.remove(server);
                    plugin.serverMap.remove(server);
                    HashMap<String, Server> serverMap = new HashMap<>(plugin.serverMap);
                    try {
                        plugin.getProxy().getServers().remove(server);
                    } catch (Exception ignored) {

                    }
                    for (String name : serverMap.keySet())
                        if (name.contains("Hub")) {
                            Server target = serverMap.get(name);
                            target.getChannel().writeAndFlush(format("SERVER_REMOVED", server));
                        }
                }
                break;
            case "PLAYER_NUMBER":
                Server updateserver = plugin.serverMap.get(server);
                updateserver.setNumberOfPlayer(Integer.parseInt((String) data));
                plugin.serverMap.put(server, updateserver);
                sendInformation(updateserver);

                break;
            case "GAME_STATUS": {
                plugin.serverMap.get(server).setStatus(String.valueOf(data));
                int count = 0;
                String serv = server.replaceAll("[0-9]", "");
                for (String key : plugin.serverMap.keySet())
                    if (key.contains(serv))
                        if (plugin.serverMap.get(key).getStatus().equalsIgnoreCase("WAITING"))
                            count++;

                if (count == 0) {
                    List<String> servers = new ArrayList<>();
                    for (String servername : plugin.serverMap.keySet())
                        if (servername.startsWith(serv))
                            servers.add(servername);

                    if (plugin.groupList.get(serv).getMaxAmount() <= servers.size()) return;
                    startGame(serv, servers.size() + 1);
                }
                sendInformation(plugin.serverMap.get(server));

                break;
            }
            case "HUB_GROUP":
                for (Server target : getHubList())
                    if (target.getChannel() != null)
                        target.getChannel().writeAndFlush(object.toJSONString());

                break;
            case "REMOVED_port":
                BungeeOTL.getInstance().serverMap.get(server).getChannel().writeAndFlush(object.toJSONString());
                break;
            case "SEND_TO_HUB": {
                ProxiedPlayer target = BungeeOTL.getInstance().getProxy().getPlayer((String) data);
                if (target == null)
                    return;
                int playerVer = target.getPendingConnection().getVersion();
                int count = -1;
                for (int i : version)
                    if (playerVer >= i)
                        count++;
                if (target.getServer() == null) {
                    Map<String, ServerInfo> map = plugin.getProxy().getServers();
                    List<String> servers = new ArrayList<>();
                    while (servers.size() == 0) {
                        for (String s : map.keySet())
                            if (s.startsWith(serverList[count]))
                                servers.add(s);
                        count--;
                    }
                    Random r = new Random();
                    ServerInfo info = map.get(servers.get(r.nextInt(servers.size())));
                    target.connect(info);
                }
                break;
            }
        }

    }

    private void a(String server, Server temp) {
        int count = 0;
        String serv = server.replaceAll("[0-9]", "");
        for (String key : plugin.serverMap.keySet()) {
            if (key.contains(serv))
                if (plugin.serverMap.get(key).getStatus().equalsIgnoreCase("WAITING"))
                    count++;
        }
        if (count == 0) {
            List<String> servers = new ArrayList<>();
            for (String servername : plugin.serverMap.keySet())
                if (servername.startsWith(serv))
                    servers.add(servername);

            if (!BungeeOTL.getInstance().hubGroups.contains(serv))
                if (plugin.groupList.get(serv).getMaxAmount() > servers.size())
                    if (!plugin.restartGroups.contains(temp.getGroup()))
                        startGame(serv, servers.size() + 1);
        }
    }

    private String format(String groupName, int onlineAmount, int maxAmount, int ram, List<String> plugins) {
        JSONObject object = new JSONObject();
        object.put("group", groupName);
        object.put("onlineAmount", onlineAmount);
        object.put("max", maxAmount);
        object.put("ram", ram);
        object.put("plugins", plugins);
        return object.toString();

    }

    private String format(String serverName, int onlineAmount, String status, int maxOnline) {
        JSONObject object = new JSONObject();
        object.put("serverName", serverName);
        object.put("onlineAmount", onlineAmount);
        object.put("status", status);
        object.put("maxOnline", maxOnline);
        return object.toString();
    }

    private static String format(String type, String data) {
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("data", data);
        return object.toString();
    }

    private static String format(String type, String data, String group) {
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("data", data);
        object.put("group", group);
        return object.toString();
    }

    public void sendMessage(Channel channel, String message) {
        channel.writeAndFlush(message);
    }

    private void sendInformation(Server updateserver) {
        for (Server target : getHubList()) {
            if (target.getChannel() != null) {
                target.getChannel().writeAndFlush(format("SEND_INFORMATION", format(updateserver.getName(), updateserver.getNumberOfPlayer(), updateserver.getStatus(), updateserver.getMaxOnline())));
            }
        }
    }

    private void serverCloed(Server updateserver) {
        for (Server target : getHubList()) {
            if (target.getChannel() != null) {
                target.getChannel().writeAndFlush(format("SERVER_CLOSED", updateserver.getName()));
            }
        }
    }

    private List<Server> getHubList() {
        List<Server> returnmap = new ArrayList<>();
        for (String name : plugin.serverMap.keySet()) {
            if (name.contains("Hub")) {
                returnmap.add(plugin.serverMap.get(name));
            }
        }
        return returnmap;
    }

    private int b(Channel channel) {
        a.putIfAbsent(channel, 0);
        return a.get(channel);
    }

    private String c(Channel channel) {
        b.putIfAbsent(channel, "");
        return b.get(channel);
    }

    private String d(Channel channel) {
        c.putIfAbsent(channel, "");
        return c.get(channel);
    }

    public static void startNewHub(int number) {
        BungeeOTL.getInstance().channelMap.get(BungeeOTL.getInstance().groupList.get("Hub").getRandomBase()).writeAndFlush(format("START_HUB", String.valueOf(number)));
    }

    private static void startGame(String name, int number) {
        BungeeOTL.getInstance().channelMap.get(BungeeOTL.getInstance().groupList.get(name).getRandomBase()).writeAndFlush(format("START_GAME", String.valueOf(number), name));
    }

    public static void stopHub(List<Server> servers) {
        stop = true;
        boolean nop = true;
        Map<String, Integer> count = new HashMap<>();
        servers.forEach(server -> {
            String group = server.getGroup().getGroupName();
            if (count.get(group) == null)
                count.put(group, 1);
            else
                count.put(group, count.get(group) + 1);
        });
        for (String group : count.keySet())
            if (count.get(group) > 1)
                nop = false;
        if (nop) {
            stop = false;
            return;
        }
        for (Server server : servers) {
            if (!(count.get(server.getGroup().getGroupName()) <= 1))
                try {
                    server.getChannel().writeAndFlush(format("STOP_SERVER", "PLAYERCOUNT"));
                } catch (NullPointerException ignored) {
                }
        }
    }

    public static void restartGroup(Group group) throws Exception {
        BungeeOTL plugin = BungeeOTL.getInstance();
        for (String s : plugin.serverMap.keySet())
            if (plugin.serverMap.get(s).getGroup().equals(group))
                plugin.serverMap.get(s).getChannel().writeAndFlush(forcestop());
        Thread.sleep(1000L);
        Channel channel;
        for (String s : group.getBase()) {
            channel = plugin.channelMap.get(s);
            channel.writeAndFlush(format("DELETE_GROUP", group.getGroupName()));
            Thread.sleep(1000L);
            channel.writeAndFlush(format("SEND_GROUP", instance.format(group.getGroupName(), group.getOnlineAmount(), group.getMaxAmount(), group.getRamInMegabyte(), group.getPluginsList())));
            plugin.restartGroups.remove(group);
        }
    }

    public static void restartServer(Server server) throws Exception {
        server.getChannel().writeAndFlush(forcestop());
        Thread.sleep(1000L);
        startGame("", 0);
    }

    public static void memory() {
        BungeeOTL plugin = BungeeOTL.getInstance();
        plugin.channelMap.keySet().forEach(base -> {
            if (base.startsWith("BASE"))
                plugin.channelMap.get(base).writeAndFlush(format("MEMORY", null));
        });
    }

    private static String forcestop() {
        return format("FORCED_STOP", null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        for (String name : plugin.channelMap.keySet()) {
            if (plugin.channelMap.get(name).closeFuture() == ctx.channel().closeFuture()) {
                System.out.println(name + " has disconnected");
                plugin.channelMap.remove(name);
                plugin.serverMap.remove(name);
            }
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getCause());
        super.exceptionCaught(ctx, cause);
    }
}
