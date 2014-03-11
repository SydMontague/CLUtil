package de.craftlancer.clutil.buildings;

public enum FeatureType
{
    XPTOBOTTLE;

    public static FeatureType getFeatureType(String type)
    {
        for(FeatureType f : values())
            if(type.equalsIgnoreCase(f.name()))
                return f;

        return null;
    }
    
}
