package fr.dinnerwolph.otl.base.netty;

import fr.dinnerwolph.otl.base.Base;
import fr.dinnerwolph.otl.base.server.Group;
import fr.dinnerwolph.otl.base.utils.MemoryUtils;
import fr.dinnerwolph.otl.base.utils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dinnerwolph
 */

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final Base base;
    private static ChannelHandlerContext context;
    private List<Integer> listPort = new ArrayList<>();
    private Map<Channel, Integer> a = new ConcurrentHashMap<>();
    private Map<Channel, String> b = new ConcurrentHashMap<>();
    private Map<Channel, String> c = new ConcurrentHashMap<>();

    public ClientHandler(Base base) {
        this.base = base;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
        format();
        Base.info("Successfully connected to OTL socket !");
        FileUtils.deleteDirectory(new File(base.getConfig().getServers()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        b.put(ctx.channel(), c(ctx.channel()) + msg);
        a(ctx.channel());
    }

    private void format() {
        Map<Object, Object> map = new HashMap<>();
        map.put("server", base.getName());
        map.put("type", "BASE_HANDSHAKE");
        map.put("data", null);
        try {
            sendMessage(JSONObject.toJSONString(map));
        } catch (Exception e) {
            e.printStackTrace();
            base.reconnect();
        }
    }

    private void sendMessage(String message) {
        context.writeAndFlush(message);
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
                        decode((JSONObject) JSONValue.parse(d(channel)), channel);
                    } catch (Exception e) {
                        Base.error("Error while parsing JSON message: " + d(channel));
                        e.printStackTrace();
                    }

                    c.put(channel, "");
                }
            }
        }
    }


    private void decode(JSONObject object, Channel channel) {
        if(base.debug)
            System.out.println(object.toJSONString());
        String type = (String) object.get("type");
        String data = (String) object.get("data");

        switch (type) {
            case "SEND_GROUP": {
                JSONObject jsonObject = (JSONObject) JSONValue.parse(data);
                long online = (Long) jsonObject.get("onlineAmount");
                long max = (Long) jsonObject.get("max");
                String group = (String) jsonObject.get("group");
                long ram = (Long) jsonObject.get("ram");
                Object o = jsonObject.get("plugins");
                System.out.println(o.getClass().getName());
                List<String> plugins = (List) o;
                Group newgroup = new Group(group, (int) online, (int) max, (int) ram, plugins);
                base.groups.put(group, newgroup);
                Map<Object, Object> map = new ConcurrentHashMap<>();
                for (int i = 1; i < online + 1; i++) {
                    int port = getPorts();
                    int number = newgroup.getNumber();
                    map.clear();
                    map.put("server", group + base.getBaseNumber() + number);
                    map.put("type", "INIT_SERVER");
                    map.put("data", port);
                    map.put("base", base.getName());
                    channel.writeAndFlush(JSONObject.toJSONString(map));
                    Utils.startServer(group + base.getBaseNumber() + number, group, port, plugins);
                }
                base.groups.put(group, newgroup);

                break;
            }
            case "START_HUB": {
                int port = getPorts();
                Group group = base.groups.get("Hub");
                Map<Object, Object> map = new ConcurrentHashMap<>();
                int number = group.getNumber();
                base.groups.put("Hub", group);
                map.put("server", "Hub" + base.getBaseNumber() + number);
                map.put("type", "INIT_SERVER");
                map.put("data", port);
                map.put("base", base.getName());
                channel.writeAndFlush(JSONObject.toJSONString(map));
                Utils.startServer("Hub" + base.getBaseNumber() + number, "Hub", port, base.groups.get("Hub").getPluginsList());

                break;
            }
            case "FORCED_STOP":
                System.exit(0);
            case "REMOVED_PORT":
                this.listPort.remove(Integer.parseInt(data));
                break;
            case "START_GAME": {
                Group group = base.groups.get(String.valueOf(object.get("group")));
                int port = getPorts();
                int number = group.getNumber();
                base.groups.put(String.valueOf(object.get("group")), group);
                Map<Object, Object> map = new ConcurrentHashMap<>();
                map.put("server", group.getGroupName() + base.getBaseNumber() + number);
                map.put("type", "INIT_SERVER");
                map.put("data", port);
                map.put("base", base.getName());
                channel.writeAndFlush(JSONObject.toJSONString(map));
                Utils.startServer(group.getGroupName() + base.getBaseNumber() + number, group.getGroupName(), port, group.getPluginsList());

                break;
            }
            case "DELETE_GROUP":
                Map<String, Group> temp = new HashMap<>(base.groups);
                for (String s1 : temp.keySet())
                    if (s1.equals(data)) {
                        base.groups.remove(s1);
                        try {
                            FileUtils.deleteDirectory(new File(Base.getInstance().getConfig().getServers() + s1 + "/"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                System.out.println("The " + data + " groups has been removed.");
                break;
            case "MEMORY":
                MemoryUtils.a();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

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

    private int getPorts() {
        for (int i = 40000; i < 50000; i++) {
            if (testPorts(i)) {
                i = addInList(i);
                return i;
            }
        }
        return 25565;
    }

    private boolean testPorts(int port) {
        if (listPort.contains(port))
            return false;
        else {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Throwable throwa = null;
                boolean value = false;
                try {
                    serverSocket.close();
                    value = true;
                } catch (Throwable throwable) {
                    throwa = throwable;
                    throwable.printStackTrace();
                } finally {
                    if (throwa != null)
                        try {
                            serverSocket.close();
                        } catch (Throwable throwable) {
                            throwa.addSuppressed(throwable);
                        }
                    else
                        serverSocket.close();
                }
                return value;
            } catch (Exception e) {
                return false;
            }
        }

    }

    private int addInList(int port) {
        if (testPorts(port)) {
            listPort.add(port);
            return port;
        } else
            return getPorts();
    }
}
