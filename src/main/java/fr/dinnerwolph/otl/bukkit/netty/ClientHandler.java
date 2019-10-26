package fr.dinnerwolph.otl.bukkit.netty;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.dinnerwolph.otl.bukkit.BukkitOTL;
import fr.dinnerwolph.otl.bukkit.event.ServerUpdateEvent;
import fr.dinnerwolph.otl.bukkit.server.Server;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dinnerwolph
 */

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final BukkitOTL plugin;
    private ChannelHandlerContext context;
    private Map<Channel, Integer> a = new HashMap();
    private Map<Channel, String> b = new HashMap();
    private Map<Channel, String> c = new HashMap();
    private static ClientHandler instance;

    public ClientHandler(BukkitOTL plugin) {
        this.plugin = plugin;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
        instance = this;
        format("SERVER_HANDSHAKE", "STARTED");
        plugin.context = context;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        b.put(ctx.channel(), c(ctx.channel()) + msg);
        a(ctx.channel());
    }

    public void a(Channel channel) {
        String[] var2 = c(channel).split("");
        int var3 = var2.length;
        for (int i = 0; i < var3; i++) {
            String var5 = var2[i];
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
                        plugin.error("Error while parsing JSON message: " + d(channel));
                        e.printStackTrace();
                    }

                    c.put(channel, "");
                }
            }
        }
    }

    public void a(JSONObject object, Channel channel) {
        if (plugin.debug)
            System.out.println(object.toJSONString());
        String server = (String) object.get("server");
        String type = (String) object.get("type");
        String data = (String) object.get("data");

        if (type.equals("STOP_SERVER")) {
            if (data.equals("PLAYERCOUNT"))
                format("STOP_SERVER", String.valueOf(Bukkit.getOnlinePlayers().size()));
            else if (data.equals("ACCEPT")) {
                format(BukkitOTL.getInstance().getSProperty("base"), "REMOVED_PORT", String.valueOf(Bukkit.getPort()));
                channel.close();
                Bukkit.shutdown();
            }

        } else if (type.equals("FORCED_STOP")) {
            format(BukkitOTL.getInstance().getSProperty("base"), "REMOVED_PORT", String.valueOf(Bukkit.getPort()));
            channel.close();
            Bukkit.shutdown();

        } else if (type.equals("SEND_INFORMATION")) {
            JSONObject dataj = (JSONObject) JSONValue.parse(data);
            String serverName = (String) dataj.get("serverName");
            long online = (long) dataj.get("onlineAmount");
            String status = (String) dataj.get("status");
            long maxOnline = (long) dataj.get("maxOnline");
            Server servers;
            if (plugin.serverList.get(serverName) == null)
                servers = new Server(serverName, (int) online, status, maxOnline);
            else
                servers = plugin.serverList.get(serverName);
            servers.setOnlineAmount((int) online);
            servers.setStatus(status);
            servers.setMaxOnline((int) maxOnline);
            plugin.serverList.put(serverName, servers);
            //required for 1.14
            Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new ServerUpdateEvent(servers)));

        } else if (type.equals("SERVER_REMOVED"))
            plugin.serverList.remove(data);

        else if (type.equals("HUB_GROUP")) {
            Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> map = new Gson().fromJson(data, mapType);
            if (map == null)
                BukkitOTL.getInstance().hubgroup.put(server, new HashMap());
            else if (BukkitOTL.getInstance().hubgroup.get(server) == null)
                BukkitOTL.getInstance().hubgroup.put(server, map);
            else {
                Map<String, String> actual = BukkitOTL.getInstance().hubgroup.get(server);
                actual.putAll(map);
                BukkitOTL.getInstance().hubgroup.put(server, actual);
            }
        } else if (type.equals("SERVER_CLOSED")) {
            plugin.serverList.remove(data);
        }
    }

    public void format(String type, String data) {
        JSONObject object = new JSONObject();
        object.put("server", plugin.getSProperty("name"));
        object.put("type", type);
        object.put("data", data);
        String s = object.toString();
        try {
            sendMessage(s);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.reconnect();
        }
    }

    public void format(String server, String type, String data) {
        JSONObject object = new JSONObject();
        object.put("server", server);
        object.put("type", type);
        object.put("data", data);
        String s = object.toString();
        try {
            sendMessage(s);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.reconnect();
        }
    }

    public void sendMessage(String message) {
        context.writeAndFlush(message);
    }

    public int b(Channel channel) {
        a.putIfAbsent(channel, 0);
        return a.get(channel);
    }

    public String c(Channel channel) {
        b.putIfAbsent(channel, "");
        return b.get(channel);
    }

    public String d(Channel channel) {
        c.putIfAbsent(channel, "");
        return c.get(channel);
    }

    public void stopServer() {
        format("SERVER_STOPPED", "");
    }

    public static void sendPlayerNumber(int number) {
        instance.format("PLAYER_NUMBER", String.valueOf(number));
    }


    public Future<String> sendMessage(String type, String data) {
        return null;
    }
}
