package fr.dinnerwolph.otl.bungee.netty;

import fr.dinnerwolph.otl.base.Base;
import fr.dinnerwolph.otl.bungee.BungeeOTL;
import fr.dinnerwolph.otl.bungee.server.Group;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.euphalys.api.player.IEuphalysPlayer;
import net.euphalys.api.plugin.IEuphalysPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.InetSocketAddress;
import java.util.*;

public class BungeeHandler extends ChannelInboundHandlerAdapter {


    private final int[] version = {47, 110, 340, 498};
    private final String[] serverList = {"Hub1-8", "Hub1-9", "Hub1-12", "Hub1-14"};
    private ChannelHandlerContext context;

    private Map<Channel, Integer> a = new HashMap<>();
    private Map<Channel, String> b = new HashMap<>();
    private Map<Channel, String> c = new HashMap<>();

    private final BungeeOTL plugin;
    private final IEuphalysPlugin api;

    public BungeeHandler(BungeeOTL plugin, IEuphalysPlugin api) {
        this.plugin = plugin;
        this.api = api;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.context = ctx;
        format();
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

    private void a(JSONObject object, Channel channel) {
        if (object == null) return;
        if (plugin.debug)
            System.out.println(object.toJSONString());
        final String type = (String) object.get("type");
        String data = (String) object.get("data");

        switch (type) {
            case "SEND_TO_HUB":
                ProxiedPlayer target = plugin.getProxy().getPlayer(data);
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
            case "ADD_SERVER":
                JSONObject jsonObject = (JSONObject) JSONValue.parse(data);
                String name = (String) jsonObject.get("serverName");
                String address = (String) jsonObject.get("address");
                long port = (long) jsonObject.get("port");
                ServerInfo serverInfo = BungeeOTL.getInstance().getProxy().constructServerInfo(name, new InetSocketAddress(address, (int) port), name, false);
                BungeeOTL.getInstance().getProxy().getServers().put(name, serverInfo);
                break;
            case "REMOVE_SERVER":
                BungeeOTL.getInstance().getProxy().getServers().remove(data);
                break;
            case "RESTART_GROUP":
                for (ProxiedPlayer player : BungeeOTL.getInstance().getProxy().getPlayers()) {
                    IEuphalysPlayer euphalysPlayer = api.getPlayer(player.getUniqueId());
                    if (euphalysPlayer.hasPermission("otl.admin"))//TODO changer la perms
                        player.sendMessage(new TextComponent("§7[§cOTL§7] §c>> " + object.get("group") + "viens de redémarrer le groupe de serveurs §4" + data));
                }
                break;
            case "SERVER_ENABLE":
                plugin.start = false;
                break;
            case "RESTART_SERVER":
                for (ProxiedPlayer player : BungeeOTL.getInstance().getProxy().getPlayers()) {
                    IEuphalysPlayer euphalysPlayer = api.getPlayer(player.getUniqueId());
                    if (euphalysPlayer.hasPermission("otl.admin"))//TODO changer la perms
                        player.sendMessage(new TextComponent("§7[§cOTL§7] §c>> " + object.get("group") + " viens de redémarrer le serveurs §4" + data));
                }
                break;
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

    private int b(Channel channel) {
        a.putIfAbsent(channel, 0);
        return a.get(channel);
    }

    private String d(Channel channel) {
        c.putIfAbsent(channel, "");
        return c.get(channel);
    }

    private String c(Channel channel) {
        b.putIfAbsent(channel, "");
        return b.get(channel);
    }

    private void format() {
        Map<Object, Object> map = new HashMap<>();
        map.put("server", plugin.network);
        map.put("type", "BUNGEE_HANDSHAKE");
        map.put("data", null);
        try {
            sendMessage(JSONObject.toJSONString(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String format(String type, String data) {
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("data", data);
        return object.toString();
    }

    private String format(String type, String groupName, String playerName) {
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("data", groupName);
        object.put("player", playerName);
        return object.toString();
    }

    private void sendMessage(String message) {
        context.writeAndFlush(message);
    }

    public void restartGroup(Group group, String name) {
        sendMessage(format("RESTART_GROUP", group.getGroupName(), name));
    }

    public void restartServer(String serverName, String name) {
        sendMessage(format("RESTART_SERVER", serverName, name));
    }
}
