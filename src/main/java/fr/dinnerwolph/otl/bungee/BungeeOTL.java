package fr.dinnerwolph.otl.bungee;

import fr.dinnerwolph.otl.bungee.commands.OTLCommands;
import fr.dinnerwolph.otl.bungee.config.Config;
import fr.dinnerwolph.otl.bungee.listener.ListenerManager;
import fr.dinnerwolph.otl.bungee.netty.ServerHandler;
import fr.dinnerwolph.otl.bungee.server.Group;
import fr.dinnerwolph.otl.bungee.server.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import net.euphalys.api.plugin.IEuphalysPlugin;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public int hubstart;
    public int port;
    public String network;
    public boolean debug = false;
    public boolean start;
    public int count;
    public int max;

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
        this.start = true;
        this.count = 0;
        this.max = 0;
    }

    @Override
    public void onEnable() {
        new Config();
        new ListenerManager();
        initnetty();
        IEuphalysPlugin plugin = (IEuphalysPlugin) getProxy().getPluginManager().getPlugin("EuphalysApi");
        getProxy().getPluginManager().registerCommand(this, new OTLCommands(plugin));
    }

    private void initnetty() {
        getProxy().getScheduler().runAsync(instance, () -> {
            info("Starting socket-server...");

            try {
                initserver();
            } catch (Exception e) {
                error("Error while initializing socket-server:");
                e.printStackTrace();
            }
        });
    }

    private void initserver() {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boosGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        final EventExecutorGroup group = new DefaultEventExecutorGroup(1500);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, 1));
                pipeline.addLast(new StringDecoder(), new StringEncoder());
                pipeline.addLast(group, "serverHandler", new ServerHandler(instance));
            }
        });

        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
