package fr.dinnerwolph.otl.bungee.config;

import fr.dinnerwolph.otl.bungee.BungeeOTL;
import fr.dinnerwolph.otl.bungee.server.Group;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dinnerwolph
 */

public class Config {

    private File file;
    private File config;
    private Map<String, Object> map;

    public Config() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void init() throws IOException {
        file = BungeeOTL.getInstance().getDataFolder();
        file.mkdirs();
        config = new File(file, "config.yml");
        if (!config.exists())
            Files.copy(getClass().getResourceAsStream("/bungee/config.yml"), config.toPath(), new CopyOption[0]);

        Yaml yaml = new Yaml();
        map = yaml.load(new FileReader(config));
        ArrayList<String> pluginsList = new ArrayList<>();
        ArrayList<String> baseList;
        Group group;
        for (String name : map.keySet()) {
            if (name.equals("port"))
                BungeeOTL.getInstance().port = (Integer) map.get(name);
            else if (name.equals("network"))
                BungeeOTL.getInstance().network = (String) map.get(name);
            else if (name.equals("HubGroups")) {
                BungeeOTL.getInstance().hubGroups = (List<String>) map.get(name);
            } else if (name.equals("debug"))
                BungeeOTL.getInstance().debug = (Boolean) map.get(name);
            else {
                LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) map.get(name);
                Object o = hashMap.get("data");
                boolean startOnInit = true;
                if (hashMap.get("startOnInit") != null)
                    startOnInit = (boolean) hashMap.get("startOnInit");
                if (o != null) {
                    LinkedHashMap map = (LinkedHashMap) o;
                    int startserver = (int) map.get("startserver");
                    if (startserver != 0)
                        BungeeOTL.getInstance().hubstart = startserver;
                    pluginsList = (ArrayList<String>) map.get("plugins");
                }
                baseList = (ArrayList<String>) hashMap.get("base");
                group = new Group(name, (Integer) hashMap.get("onlineAmount"), (Integer) hashMap.get("maxAmount"), (Integer) hashMap.get("ramInMegabyte"), baseList.toArray(new String[0]), pluginsList, (Integer) hashMap.get("maxOnline"), startOnInit);
                BungeeOTL.getInstance().groupList.put(name, group);
            }
        }
    }
}
