package de.craftlancer.clutil;

public enum ModuleType
{
    ADVENCHANTMENTS,
    EFFECTWEAPONS,
    ORESTONES,
    HEADHUNT,
    PUMPKINBANDIT, 
    OWNHEALTH, 
    HOME, 
    SPEED;
    
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
