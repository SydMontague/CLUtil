package de.craftlancer.clutil;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class Module
{
    private final CLUtil plugin;
    private final File configFile;
    private FileConfiguration config;
    
    public Module(CLUtil plugin)
    {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "modules" + File.separator + getName().getConfigName() + ".yml");
    }
    
    public FileConfiguration getConfig()
    {
        if (config == null)
            config = YamlConfiguration.loadConfiguration(configFile);
        
        return config;
    }
    
    public void saveConfig()
    {
        try
        {
            config.save(configFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void onDisable()
    {
    }
    
    public CLUtil getPlugin()
    {
        return plugin;
    }
    
    public abstract ModuleType getName();
}
