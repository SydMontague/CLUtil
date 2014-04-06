package de.craftlancer.clutil.buildings;

import java.util.Map;

import org.bukkit.block.BlockFace;

public class FeatureFactory
{
    
    public static FeatureBuilding loadFeature(String type, Map<String, RelativeLocation> blockLoc)
    {
        FeatureType ftype = FeatureType.getFeatureType(type);
        
        if (ftype == null)
            throw new IllegalArgumentException("Invalid FeatureType");
        
        switch (ftype)
        {
            case XPTOBOTTLE:
                return new XpToBottleFeature(blockLoc, null);
                
        }
        return null;
    }
    
    public static FeatureBuilding createFeature(FeatureType type, Map<String, BlockWrapper> blockLoc, BlockFace facing)
    {
        switch (type)
        {
            case XPTOBOTTLE:
                return new XpToBottleFeature(blockLoc, facing);
        }
        return null;
    }
}
