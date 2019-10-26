package fr.dinnerwolph.otl.base.utils;

import com.google.common.io.Files;
import fr.dinnerwolph.otl.base.Base;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Dinnerwolph
 */

public class Utils {

    public static void startServer(String name, String group, int port, List<String> pluginsList) {
        Base.info("Starting server " + name + "....");
        double time = System.currentTimeMillis();
        File file = new File(Base.getInstance().getConfig().getTemplates(), group);
        if (!file.exists()) {
            file = b(group);
            if (file == null) {
                Base.error("Could not start server " + name + ": No template called " + group + " found.");
                return;
            }
        }

        File spigot = new File(file, "spigot.jar");
        if (!spigot.exists()) {
            spigot = new File(Base.getInstance().getConfig().getPlugins(), "spigot.jar");
            if (!spigot.exists())
                Base.error("Could not start server " + name + " because spigot.jar does not exist.");
            else {
                generateServer(name, file, spigot, time, group, port, pluginsList);
            }
        } else {
            generateServer(name, file, spigot, time, group, port, pluginsList);
        }
    }

    private static File b(String var1) {
        File file = new File(Base.getInstance().getConfig().getTemplates());
        ArrayList<File> list = new ArrayList<>();
        File[] files = file.listFiles();
        for (File var2 : files) {
            if (var2.isDirectory() && var2.getName().startsWith(var1 + "_"))
                list.add(var2);
        }

        if (list.size() < 1)
            return null;
        else
            return list.get(new Random().nextInt(list.size()));

    }

    private static void generateServer(String name, File file, File spigot, double time, String group, int port, List<String> pluginsList) {
        File target = new File(Base.getInstance().getConfig().getServers() + group + "/" + name);
        try {
            if (target.exists())
                target.delete();
            FileUtils.copyDirectory(file, target);
            FileUtils.copyFile(spigot, new File(target, "spigot.jar"));
            for (String plugin : pluginsList) {
                if (new File(Base.getInstance().getConfig().getPlugins(), plugin + ".jar").exists())
                    FileUtils.copyFile(new File(Base.getInstance().getConfig().getPlugins(), plugin + ".jar"), new File(target + "/plugins/", plugin + ".jar"));
                else
                    throw new FileNotFoundException("The plugin " + plugin + " has not found in plugins folder.");
            }

            File otl = new File(new File(target, "/plugins/"), Base.getInstance().a());
            if (otl.exists())
                otl.delete();
            try {
                FileUtils.copyFile(new File(Base.class.getProtectionDomain().getCodeSource().getLocation().getPath()), otl);
            } catch (Exception e) {
                Base.error("Error while copying plugin into template:");
                e.printStackTrace();
                if (!otl.exists())
                    return;
            }

            File plugins = new File("plugins/");
            if (plugins.exists() && plugins.isDirectory()) {
                File[] files = plugins.listFiles();
                for (File temp : files) {
                    if (pluginsList.contains(temp.getName())) {
                        File targetpl = new File(target, "plugins/" + temp.getName());
                        if (!targetpl.exists())
                            Files.copy(temp, targetpl);
                    }
                }
            }

            double finish = System.currentTimeMillis();
            Base.info("Successfully prepared starting server " + name + " in " + (finish - time) / 1000.0D + " seconds.");
        } catch (Exception e) {
            Base.error("Error while starting server " + name + ":");
            e.printStackTrace();
        }
        ProcessBuilder builder = (new ProcessBuilder(new String[]{"/bin/bash", "-c", "screen -mdS " + name + " java -Xmx" + Base.getInstance().groups.get(group).getRamInMegabyte() + "M -Dbase=" + Base.getInstance().getName() + " -Dname=" + name + " -Daddress=" + Base.getInstance().address + " -Dport=" + Base.getInstance().port + " -jar spigot.jar -p " + port})).directory(target);

        try {
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String reponse;
            while ((reponse = reader.readLine()) != null)
                Base.info("Got response whene starting server: " + reponse);
        } catch (Exception e) {
            Base.error("Error while starting server " + name + ":");
            e.printStackTrace();
        }
    }
}
