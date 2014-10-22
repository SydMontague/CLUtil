package de.craftlancer.clutil.old.buildings;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clutil.CLUtil;

public class FeatureManager
{
    private static FeatureManager instance;
    
    private Set<FeatureInstance> features = new HashSet<FeatureInstance>();
    
    private Plugin plugin;
    private File configFile;
    private FileConfiguration config;
    
    private FeatureManager(Plugin plugin)
    {
        configFile = new File(plugin.getDataFolder(), "buildings.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        
        this.plugin = plugin;
        
        loadFeatures();
    }
    
    public static synchronized FeatureManager getInstance()
    {
        if (instance == null)
            instance = new FeatureManager(CLUtil.getInstance());
        
        return instance;
    }
    
    private void loadFeatures()
    {
        for (String key : config.getKeys(false))
        {
            FeatureInstance feature = createFeatureInstance(key);
            if (feature == null)
                continue;
            
            addFeature(feature);
        }
    }
    
    public void addFeature(FeatureInstance feature)
    {
        plugin.getServer().getPluginManager().registerEvents(feature, plugin);
        features.add(feature);
    }
    
    private FeatureInstance createFeatureInstance(String key)
    {
        FeatureType type = FeatureType.getFeatureType(config.getString(key + ".type"));
        
        if (type == null)
        {
            plugin.getLogger().severe("Feature " + key + " has no valid type!");
            return null;
        }
        
        switch (type)
        {
            case XPTOBOTTLE:
                return new XpToBottleInstance(key, config);
            case CLASSCHANGE:
                return new ClassChangeInstance(key, config);
            default:
                plugin.getLogger().warning("Unhandled, but valid, FeatureType detected! Inform the author please.");
                return null;
        }
    }
    
    public void saveAll()
    {
        for (FeatureInstance feature : features)
            feature.save(config);
        
        try
        {
            config.save(configFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
