package fr.dinnerwolph.otl.bungee;

import fr.dinnerwolph.otl.bungee.commands.OTLCommands;
import fr.dinnerwolph.otl.bungee.config.Config;
import fr.dinnerwolph.otl.bungee.listener.ListenerManager;
import fr.dinnerwolph.otl.bungee.netty.BungeeHandler;
import fr.dinnerwolph.otl.bungee.netty.ServerHandler;
import fr.dinnerwolph.otl.bungee.server.Group;
import fr.dinnerwolph.otl.bungee.server.Server;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import net.euphalys.api.plugin.IEuphalysPlugin;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Dinnerwolph
 */

public class BungeeOTL extends Plugin {

    private static BungeeOTL instance;
    public Map<String, Group> groupList;
    public Map<String, Channel> channelMap;
    public Map<String, Server> serverMap;
    public List<String> hubGroups;
    public List<Group> restartGroups;
    public List<String> serverList;
    public int hubstart;
    public int port;
    public String network;
    public boolean debug = true;
    public boolean start;
    public int count;
    public int max;
    public BungeeHandler handler;

    public static void info(String info) {
        instance.getLogger().info(info);
    }

    public static void error(String error) {
        instance.getLogger().warning(error);
    }

    public static BungeeOTL getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        instance = this;
        this.groupList = new ConcurrentHashMap<>();
        this.channelMap = new ConcurrentHashMap<>();
        this.serverMap = new ConcurrentHashMap<>();
        this.restartGroups = new ArrayList<>();
        this.serverList = new ArrayList<>();
        this.start = true;
        this.count = 0;
        this.max = 0;
    }

    @Override
    public void onEnable() {
        new Config();
        new ListenerManager();
        IEuphalysPlugin plugin = (IEuphalysPlugin) getProxy().getPluginManager().getPlugin("EuphalysApi");
        new Thread() {
            @Override
            public void run() {
                initnetty(plugin);
            }
        }.start();
        getProxy().getPluginManager().registerCommand(this, new OTLCommands(plugin));
    }

    private void initnetty(IEuphalysPlugin plugin) {
        NioEventLoopGroup workergroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workergroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(new StringEncoder(), new StringDecoder(), handler = new BungeeHandler(instance, plugin));
                }
            });
            ChannelFuture future = null;
            try {
                future = bootstrap.connect("127.0.0.1", 5000).sync();
            } catch (Exception e) {
                //reconnect();
            }

            try {
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                //reconnect();
            }
        } finally {
            workergroup.shutdownGracefully();
            for(String serverInfo : serverList) {
                getProxy().getServers().remove(serverInfo);

            }
            this.groupList = new ConcurrentHashMap<>();
            this.channelMap = new ConcurrentHashMap<>();
            this.serverMap = new ConcurrentHashMap<>();
            this.restartGroups = new ArrayList<>();
            this.serverList = new ArrayList<>();
            reconnect(plugin);
        }
    }

    public void reconnect(IEuphalysPlugin plugin) {
        info("Disconnected from server. Reconnecting...");
        getProxy().getScheduler().schedule(instance, () -> {
            initnetty(plugin);
        }, 3, TimeUnit.SECONDS);
    }
}
