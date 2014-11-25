package de.craftlancer.clutil;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Module
{
    private final CLUtil plugin;
    private final File configFile;
    private FileConfiguration config;
    
    public Module(CLUtil plugin)
    {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "modules" + File.separator + getType().getConfigName() + ".yml");
    }
    
    public FileConfiguration getConfig()
    {
        if (config == null)
            config = YamlConfiguration.loadConfiguration(configFile);
        
        return config;
    }
    
    public void saveConfig()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
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
        }.runTaskAsynchronously(getPlugin());
    }
    
    public void debug(String message)
    {
        getPlugin().getDebugModule().debug(getType().name(), message);
    }
    
    public void onDisable()
    {
    }
    
    public CLUtil getPlugin()
    {
        return plugin;
    }
    
    public abstract ModuleType getType();
}
