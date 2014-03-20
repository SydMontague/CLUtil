package de.craftlancer.clutil.buildings;

import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import de.craftlancer.groups.Town;
import de.craftlancer.groups.managers.TownManager;

/*
 * void:
 *   type: <type>
 *   town: <town>
 * 
 */
public abstract class FeatureInstance implements Listener
{
    private UUID uuid;
    private Town hostTown;
    
    public FeatureInstance(Town hostTown)
    {
        uuid = UUID.randomUUID();
        this.hostTown = hostTown;
    }
    
    public FeatureInstance(String key, FileConfiguration config)
    {
        uuid = UUID.fromString(key);
        hostTown = TownManager.getTown(config.getString(key + ".town"));
        
    }
    
    public abstract FeatureType getFeatureType();
    
    public Town getTown()
    {
        return hostTown;
    }
    
    public UUID getUniqueId()
    {
        return uuid;
    }
    
    public void save(FileConfiguration config)
    {
        String key = uuid.toString();
        config.set(key + ".town", hostTown.getName());
        config.set(key + ".type", getFeatureType().name());
    }
}
