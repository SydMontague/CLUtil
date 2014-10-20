package de.craftlancer.clutil.old.buildings;

public enum FeatureType
{
    XPTOBOTTLE,
    CLASSCHANGE;
    
    public static FeatureType getFeatureType(String type)
    {
        if (type == null)
            return null;
        
        for (FeatureType f : values())
            if (type.equalsIgnoreCase(f.name()))
                return f;
        
        return null;
    }
    
}
