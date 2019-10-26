package fr.dinnerwolph.otl.base.config;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.Map;

/**
 * @author Dinnerwolph
 */

public class Config {

    private final String configs = "configs/";
    private final String templates = "templates/";
    private final String plugins = "plugins/";
    private final String servers = "servers/";
    private File file;
    private Map<String, Object> map;

    public Config() {
        init();
    }

    private void init() {
        try {
            File servers = new File(getServers());
            FileUtils.deleteDirectory(servers);
            new File(getConfigs()).mkdirs();
            new File(getTemplates()).mkdirs();
            new File(getPlugins()).mkdirs();
            new File(getServers()).mkdirs();
            file = new File(getConfigs(), "config.yml");
            if (!file.exists())
                Files.copy(getClass().getResourceAsStream("/base/config.yml"), file.toPath(), new CopyOption[0]);
            Yaml yaml = new Yaml();
            map = yaml.load(new FileReader(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getConfigs() {
        return configs;
    }

    public String getTemplates() {
        return templates;
    }

    public String getPlugins() {
        return plugins;
    }

    public String getServers() {
        return servers;
    }

    public File getFile() {
        return file;
    }

    public Map<String, Object> getMap() {
        return map;
    }
}
