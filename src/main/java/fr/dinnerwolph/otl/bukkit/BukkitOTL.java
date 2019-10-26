package fr.dinnerwolph.otl.bukkit;

import fr.dinnerwolph.otl.bukkit.listener.ListenerManager;
import fr.dinnerwolph.otl.bukkit.netty.ClientHandler;
import fr.dinnerwolph.otl.bukkit.server.Server;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dinnerwolph
 */

public class BukkitOTL extends JavaPlugin {

    private static BukkitOTL instance;
    public Map<String, Server> serverList = new HashMap();
    public Map<String, Map<String, String>> hubgroup = new HashMap();
    public ChannelHandlerContext context;
    public final boolean debug = false;

    public static void info(String info) {
        System.out.println(info);
    }

    public static void error(String error) {
        System.err.println(error);
    }

    public static BukkitOTL getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        new Thread() {
            @Override
            public void run() {
                connect();
            }
        }.start();
        instance = this;
        new ListenerManager();
    }

    private void connect() {
        NioEventLoopGroup workergroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workergroup);
            bootstrap.channel(NioSocketChannel.class);
            final EventExecutorGroup group = new DefaultEventExecutorGroup(1500);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(group, new StringEncoder(), new StringDecoder(), new ClientHandler(instance));
                }
            });
            ChannelFuture future = null;
            try {
                future = bootstrap.connect(getSProperty("address"), Integer.parseInt(getSProperty("port"))).sync();
            } catch (Exception e) {
                reconnect();
            }

            try {
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                reconnect();
            }
        } finally {
            workergroup.shutdownGracefully();
            reconnect();
        }
    }

    public void reconnect() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            plugin.onDisable();
        Bukkit.shutdown();
    }

    public String getSProperty(String property) {
        return System.getProperty(property);
    }
}
