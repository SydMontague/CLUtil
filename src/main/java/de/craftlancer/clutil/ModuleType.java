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
    SPEED,
    DISTANCESHOT,
    RANDOMCHESTS,
    CUSTOMENCHANTS,
    TOKEN,
    TRACKING,
    DEBUG,
    WORKINGSKILLS,
    POTIONREBALANCE,
    PVPREBALANCE,
    COLOREDNAMETAGS,
    GRIEFBLOCK,
    UTILITY,
    CAPTURETHETOKEN,
    CLASSCHANGE,
    ENDERBALL,
    REMOVEMETA;
    
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
