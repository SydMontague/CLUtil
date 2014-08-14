package de.craftlancer.clutil;

public enum ModuleType
{
    ADVENCHANTMENTS,
    EFFECTWEAPONS, 
    ORESTONES, 
    HEADHUNT;
    
    private final String config;
    
    private ModuleType(String config)
    {
        this.config = config;
    }
    
    private ModuleType()
    {
        this.config = name();
    }
    
    public String getConfigName()
    {
        return config;
    }
}
