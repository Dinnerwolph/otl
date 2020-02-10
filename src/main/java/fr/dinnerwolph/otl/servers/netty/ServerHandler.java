package fr.dinnerwolph.otl.servers.netty;

import fr.dinnerwolph.otl.servers.OTLServer;
import fr.dinnerwolph.otl.servers.server.Group;
import fr.dinnerwolph.otl.servers.server.Server;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final OTLServer plugin;
    private boolean stop;

    public ServerHandler() {
        this.plugin = OTLServer.instance;
        stop = false;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        JSONObject object = (JSONObject) JSONValue.parse(msg.toString());
        System.out.println(object.toJSONString());
        final String server = (String) object.get("server");
        final String type = (String) object.get("type");
        Object data = object.get("data");

        switch (type) {
            case "BASE_HANDSHAKE":
                if (OTLServer.instance.channelMap.containsKey(server))
                    System.out.println("The base " + server + " is already registered");
                else {
                    ChannelFuture future = ctx.channel().closeFuture();
                    future.addListener((futurel) -> {
                        for (String name : plugin.channelMap.keySet()) {
                            if (plugin.channelMap.get(name).closeFuture() == futurel) {
                                System.out.println(name + " has disconnected");
                                plugin.channelMap.remove(name);
                                //plugin.serverMap.remove(name);
                            }
                        }
                    });

                    plugin.channelMap.put(server, ctx.channel());
                    for (Group group : plugin.groupList.values()) {
                        if (group.isBaseGroup(server) && group.isStartOnInit()) {
                            ctx.channel().writeAndFlush(format("SEND_GROUP", format(group.getGroupName(), group.getOnlineAmount(), group.getMaxAmount(), group.getRamInMegabyte(), group.getPluginsList())));
                            plugin.max += group.getOnlineAmount();
                        }
                    }
                }
                break;
            case "BUNGEE_HANDSHAKE":
                if (plugin.channelMap.containsKey(server))
                    System.out.println("The Bungee " + server + " is already registered");
                else {
                    ChannelFuture future = ctx.channel().closeFuture();
                    future.addListener((futurel) -> {
                        Map<String, Channel> tempMap = new HashMap<>(plugin.channelMap);
                        for (String name : tempMap.keySet()) {
                            if (tempMap.get(name).closeFuture() == futurel) {
                                System.out.println(name + " has disconnected");
                                plugin.channelMap.remove(name);
                                //plugin.serverMap.remove(name);
                            }
                        }
                    });

                    plugin.channelMap.put(server, ctx.channel());
                    for (Server server1 : plugin.serverMap.values()) {
                        ctx.writeAndFlush(format("ADD_SERVER", format(server1.getName(), ((InetSocketAddress) server1.getChannel().remoteAddress()).getAddress().getHostAddress(), server1.getPort())));
                    }
                    if(!plugin.start)
                        ctx.writeAndFlush(format("SERVER_ENABLE", null));
                }
                break;
            case "INIT_SERVER": {
                Group group = null;
                for (Group groups : plugin.groupList.values())
                    if (server.contains(groups.getGroupName()))
                        group = groups;
                long port = (Long) data;
                Server servers = new Server(server, group, (int) port, (String) object.get("base"), group.getMaxOnline());
                plugin.tempMap.put(server, servers);
                break;
            }
            case "SERVER_HANDSHAKE": {
                ChannelFuture future = ctx.channel().closeFuture();
                future.addListener((futurel) -> {
                    try {
                        for (String name : plugin.channelMap.keySet()) {
                            if (plugin.channelMap.get(name).closeFuture() == future) {
                                System.out.println(name + " has disconnected");
                                plugin.channelMap.remove(name);
                                for (Channel channel : getProxyList())
                                    channel.writeAndFlush(format("REMOVE_SERVER", name));
                                Server temp = plugin.serverMap.get(name);
                                serverClosed(temp);
                                plugin.serverMap.remove(name);
                                //TODO à voir car ça pose problème au restartgroup a(name, temp);
                            }
                        }
                    } catch (Exception ignored) {

                    }
                });

                plugin.channelMap.put(server, ctx.channel());
                Server servers = plugin.tempMap.get(server);
                servers.setChannel(ctx.channel());
                servers.setStatus(String.valueOf(data));
                plugin.serverMap.put(server, servers);
                plugin.tempMap.remove(server);
                plugin.count++;
                if (plugin.count >= plugin.max) {
                    plugin.start = false;
                    for (Channel channel : getProxyList())
                        channel.writeAndFlush(format("SERVER_ENABLE", null));
                }
                for (String serverkey : plugin.serverMap.keySet())
                    sendInformation(plugin.serverMap.get(serverkey));
                for (Channel channel : getProxyList())
                    channel.writeAndFlush(format("ADD_SERVER", format(servers.getName(), ((InetSocketAddress) servers.getChannel().remoteAddress()).getAddress().getHostAddress(), servers.getPort())));

                break;
            }
            case "STOP_SERVER":
                if (stop && Integer.parseInt((String) data) == 0) {
                    stop = false;
                    ctx.channel().writeAndFlush(format("STOP_SERVER", "ACCEPT"));
                    plugin.channelMap.remove(server);
                    plugin.serverMap.remove(server);
                    HashMap<String, Server> serverMap = new HashMap<>(plugin.serverMap);
                    for (Channel proxy : getProxyList()) {
                        //TODO send remove server to proxies
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
                    if (key.contains(serv)) {
                        System.out.println(key + ": " + plugin.serverMap.get(key).getStatus());
                        if (plugin.serverMap.get(key).getStatus().equals("WAITING") || plugin.serverMap.get(key).getStatus().equals("INIT_SERVER") || plugin.serverMap.get(key).getStatus().equals("STARTED"))
                            count++;
                    }
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
                plugin.serverMap.get(server).getChannel().writeAndFlush(object.toJSONString());
                break;
            case "SEND_TO_HUB":
                for (Channel channel : getProxyList())
                    channel.writeAndFlush(format(type, data.toString()));
                break;
            case "RESTART_GROUP":
                Group group = plugin.groupList.get(data);
                for (Server servers : plugin.serverMap.values())
                    if (servers.getGroup() == group)
                        servers.getChannel().writeAndFlush(format("FORCED_STOP", null));
                plugin.groupList.remove(group.getGroupName());
                plugin.loadGroup(group.getGroupName());
                Group newGroup = plugin.groupList.get(group.getGroupName());
                for (String base : group.getBase()) {
                        plugin.channelMap.get(base).writeAndFlush(format("DELETE_GROUP", group.getGroupName()));
                    plugin.channelMap.get(base).writeAndFlush(format("SEND_GROUP", format(newGroup.getGroupName(), newGroup.getOnlineAmount(), newGroup.getMaxAmount(), newGroup.getRamInMegabyte(), newGroup.getPluginsList())));

                }
                for(Channel channel : getProxyList())
                    channel.writeAndFlush(format("RESTART_GROUP", (String) data, (String) object.get("player")));
                break;
            case "RESTART_SERVER":
                Server target = plugin.serverMap.get(data);
                target.getChannel().writeAndFlush(format("FORCED_STOP", null));
                plugin.serverMap.remove(data);
                for(Channel channel : getProxyList())
                    channel.writeAndFlush(format("RESTART_SERVER", (String) data, (String) object.get("player")));
                for(String base : target.getGroup().getBase())
                    plugin.channelMap.get(base).writeAndFlush(format("RESTART_SERVER", (String) data));
                break;
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

    private String format(String serverName, String address, int port) {
        JSONObject object = new JSONObject();
        object.put("serverName", serverName);
        object.put("address", address);
        object.put("port", port);
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

    private void sendInformation(Server updateserver) {
        for (Server target : getHubList()) {
            if (target.getChannel() != null) {
                target.getChannel().writeAndFlush(format("SEND_INFORMATION", format(updateserver.getName(), updateserver.getNumberOfPlayer(), updateserver.getStatus(), updateserver.getMaxOnline())));
            }
        }
    }

    private void serverClosed(Server updateserver) {
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

    private List<Channel> getProxyList() {
        List<Channel> returnList = new ArrayList<>();
        for (String name : plugin.channelMap.keySet())
            if (name.contains("Proxy"))
                returnList.add(plugin.channelMap.get(name));
        return returnList;
    }

    private void startGame(String name, int number) {
        plugin.channelMap.get(plugin.groupList.get(name).getRandomBase()).writeAndFlush(format("START_GAME", String.valueOf(number), name));
    }
}
