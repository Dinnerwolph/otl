package fr.dinnerwolph.otl.base.server;

import java.util.List;

/**
 * @author Dinnerwolph
 */

public class Group {

    private final String groupName;
    private final int onlineAmount;
    private final int maxAmount;
    private final int ramInMegabyte;
    private final List<String> plugins;
    private int number;

    public Group(String groupName, int onlineAmount, int maxAmount, int ram, List<String> plugins) {
        this.groupName = groupName;
        this.onlineAmount = onlineAmount;
        this.maxAmount = maxAmount;
        this.ramInMegabyte = ram;
        this.plugins = plugins;
        this.number = 0;
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

    public List<String> getPluginsList() {
        return plugins;
    }

    public int getNumber() {
        number++;
        return number;
    }
}
