package fr.dinnerwolph.otl.bungee.commands;

import fr.dinnerwolph.otl.bungee.BungeeOTL;
import fr.dinnerwolph.otl.bungee.netty.ServerHandler;
import fr.dinnerwolph.otl.bungee.server.Group;
import fr.dinnerwolph.otl.bungee.server.Server;
import net.euphalys.api.player.IEuphalysPlayer;
import net.euphalys.api.plugin.IEuphalysPlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OTLCommands extends Command implements TabExecutor {
    private final IEuphalysPlugin plugin;

    public OTLCommands(IEuphalysPlugin plugin) {
        super("otl");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length == 0)
            displayHelp(commandSender);
        else {
            if (args[0].equals("restartgroup")) {
                if (args.length >= 2) {
                    Group group = BungeeOTL.getInstance().groupList.get(args[1]);
                    if (group == null)
                        commandSender.sendMessage(new TextComponent("§cErreur : Ce groupe n'existe pas."));
                    else {
                        try {
                            ServerHandler.restartGroup(group);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        for (ProxiedPlayer player : BungeeOTL.getInstance().getProxy().getPlayers()) {
                            IEuphalysPlayer euphalysPlayer = plugin.getPlayer(player.getUniqueId());
                            if (euphalysPlayer.hasPermission("otl.admin"))//TODO changer la perms
                                player.sendMessage(new TextComponent("§7[§cOTL§7] §c>> " + commandSender.getName() + "viens de redémarrer le groupe de serveurs §4" + group.getGroupName()));
                        }
                    }
                }
            } else if (args[0].equals("restartserver")) {
                commandSender.sendMessage(new TextComponent("cmd en dev :/"));
                if (true)    //TODO remove this
                    return;
                Server server = BungeeOTL.getInstance().serverMap.get(args[1]);
                if (server == null)
                    commandSender.sendMessage(new TextComponent("§cErreur : Ce serveur n'existe pas."));
                else {
                    for (ProxiedPlayer player : BungeeOTL.getInstance().getProxy().getPlayers()) {
                        IEuphalysPlayer euphalysPlayer = plugin.getPlayer(player.getUniqueId());
                        if (euphalysPlayer.hasPermission("otl.admin"))//TODO changer la perms
                            player.sendMessage(new TextComponent("§7[§cOTL§7] §c>> " + commandSender.getName() + " viens de redémarrer le serveurs §4" + server.getName()));
                    }
                }
            } else if (args[0].equals("memory")) {
                ServerHandler.memory();
            } else
                displayHelp(commandSender);
        }

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        Set<String> matches = new HashSet<>();
        if (args.length == 1) {
            String search = args[0].toLowerCase(Locale.ROOT);
            if ("restartgroup".toLowerCase(Locale.ROOT).startsWith(search))
                matches.add("restartgroup");
            if ("restartserver".toLowerCase(Locale.ROOT).startsWith(search))
                matches.add("restartserver");
            if ("memory".toLowerCase(Locale.ROOT).startsWith(search))
                matches.add("memory");
        }
        if (args.length == 2) {
            String search = args[1].toLowerCase(Locale.ROOT);
            for (String serverName : BungeeOTL.getInstance().groupList.keySet()) {
                if (serverName.toLowerCase(Locale.ROOT).startsWith(search)) {
                    matches.add(serverName);
                }
            }
        }
        return matches;
    }

    private void displayHelp(CommandSender sender) {
        sender.sendMessage(new TextComponent("§cUsage : /otl restartgroup <GroupName>"));
        sender.sendMessage(new TextComponent("§cUsage : /otl restartserver <ServerName>"));
        sender.sendMessage(new TextComponent("§cUsage : /otl memory"));
    }

    private static String format(String data) {
        JSONObject object = new JSONObject();
        object.put("type", "SEND_GROUP");
        object.put("data", data);
        return object.toString();
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
}
