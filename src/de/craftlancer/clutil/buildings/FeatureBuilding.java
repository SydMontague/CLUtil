package de.craftlancer.clutil.buildings;

import org.bukkit.block.Block;

import de.craftlancer.groups.Town;

public interface FeatureBuilding
{
    public void place(Block block, Town town, int playerFacing);
    
    public String getName();
}
