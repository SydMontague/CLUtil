package de.craftlancer.clutil.buildings;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import de.craftlancer.groups.Town;

public interface FeatureBuilding
{
    public void place(Block block, Town town, int playerFacing);
    
    public String getName();
    
    public FeatureType getType();

    public void save(String name, FileConfiguration config);
}
