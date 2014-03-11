package de.craftlancer.clutil.buildings;

import java.util.Map;

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
                return new XpToBottleFeature(blockLoc);
                
        }
        // TODO Auto-generated method stub
        return null;
    }
    
}
