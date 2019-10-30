package fr.dinnerwolph.otl.base;

import fr.dinnerwolph.otl.base.config.Config;
import fr.dinnerwolph.otl.base.netty.ClientHandler;
import fr.dinnerwolph.otl.base.server.Group;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dinnerwolph
 */

public class Base {
    private static Base instance;
    private Config config;
    public String address;
    public int port;
    public Map<String, Group> groups = new HashMap();
    public final boolean debug = true;

    public static void info(String info) {
        System.out.println(info);
    }

    public static void error(String error) {
        System.err.println(error);
    }

    public static Base getInstance() {
        return instance;
    }

    public Base() {
        init();
    }

    private void init() {
        instance = this;
        config = new Config();
        address = (String) config.getMap().get("address");
        port = (Integer) config.getMap().get("port");
        initnetty();
    }

    private void initnetty() {
        NioEventLoopGroup workergroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workergroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(new StringEncoder(), new StringDecoder(), new ClientHandler(instance));
                }
            });
            ChannelFuture future = null;
            try {
                future = bootstrap.connect(address, port).sync();
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
        info("Disconnected from server. Reconnecting...");
        try {
            Thread.sleep(3000L);
        } catch (Exception e) {
            error("Please do not interrupt while waiting.");
        }
        initnetty();
    }

    public String getName() {
        return (String) config.getMap().get("name");
    }

    public int getBaseNumber() {
        return  Integer.parseInt(getName().replace("BASE-", ""));
    }

    public Config getConfig() {
        return config;
    }

    public String a() {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getName();
    }
}
