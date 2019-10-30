package fr.dinnerwolph.otl.servers.server;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Dinnerwolph
 */

public class Group {

    private final String groupName;
    private final int onlineAmount;
    private final int maxAmount;
    private final int ramInMegabyte;
    private String[] base;
    private final ArrayList<String> pluginsList;
    private final int maxOnline;
    private boolean startOnInit;

    public Group(String groupName, int onlineAmount, int maxAmount, int ramInMegabyte, String[] base, ArrayList<String> pluginsList, int maxOnline, boolean startOnInit) {
        this.groupName = groupName;
        this.onlineAmount = onlineAmount;
        this.maxAmount = maxAmount;
        this.ramInMegabyte = ramInMegabyte;
        this.base = base;
        this.pluginsList = pluginsList;
        this.maxOnline = maxOnline;
        this.startOnInit = startOnInit;
    }


    public String getGroupName() {
        return groupName;
    }

    public int getOnlineAmount() {
        return onlineAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public int getRamInMegabyte() {
        return ramInMegabyte;
    }

    public String[] getBase() {
        return base;
    }

    public ArrayList<String> getPluginsList() {
        return pluginsList;
    }

    public int getMaxOnline() {
        return maxOnline;
    }

    public boolean isBaseGroup(String basename) {
        for (int i = 0; i < base.length; i++) {
            if (base[i].equals(basename))
                return true;
        }
        return false;
    }

    public String getRandomBase() {
        int r = new Random().nextInt(base.length);
        return base[r];
    }

    public boolean isStartOnInit() {
        return startOnInit;
    }
}