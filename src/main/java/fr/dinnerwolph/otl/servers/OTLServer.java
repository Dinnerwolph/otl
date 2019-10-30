package fr.dinnerwolph.otl.servers;

import fr.dinnerwolph.otl.servers.server.Group;
import fr.dinnerwolph.otl.servers.server.Server;
import fr.dinnerwolph.otl.servers.netty.ServerHandler;
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
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.*;

public class OTLServer {

    public static OTLServer instance;
    public Map<String, Channel> channelMap;
    public Map<String, Server> serverMap;
    public Map<String, Server> tempMap;
    public Map<String, Group> groupList;
    public List<String> hubGroups;
    public int max;
    public int count;
    public boolean start;
    public File file;
    public Map<String, Object> config;
    public int hubstart;

    public OTLServer() throws IOException {
        instance = this;
         channelMap = new HashMap<>();
         serverMap = new HashMap<>();
         groupList = new HashMap<>();
         tempMap = new HashMap<>();
         count = 0;
         start = true;
         file = new File("config.yml");
         if(!file.exists())
             Files.copy(getClass().getResourceAsStream("/bungee/config.yml"), file.toPath(), new CopyOption[0]);
         config = new Yaml().load(new FileReader(file));
         Group group;
         List<String> baseList;
         ArrayList<String> pluginsList = new ArrayList<>();
         for(String name : config.keySet()) {
             if(name.equals("HubGroups")) {
                hubGroups = (List<String>) config.get(name);
             }else {
                 LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) config.get(name);
                 Object o = hashMap.get("data");
                 boolean startOnInit = true;
                 if (hashMap.get("startOnInit") != null)
                     startOnInit = (boolean) hashMap.get("startOnInit");
                 if (o != null) {
                     LinkedHashMap map = (LinkedHashMap) o;
                     int startserver = (int) map.get("startserver");
                     if (startserver != 0)
                         hubstart = startserver;
                     pluginsList = (ArrayList<String>) map.get("plugins");
                 }
                 baseList = (ArrayList<String>) hashMap.get("base");
                 group = new Group(name, (Integer) hashMap.get("onlineAmount"), (Integer) hashMap.get("maxAmount"), (Integer) hashMap.get("ramInMegabyte"), baseList.toArray(new String[0]), pluginsList, (Integer) hashMap.get("maxOnline"), startOnInit);
                 groupList.put(name, group);
             }
         }
     initserver();
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
                pipeline.addLast(group, "serverHandler", new ServerHandler());
            }
        });

        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            bootstrap.bind(5000).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
