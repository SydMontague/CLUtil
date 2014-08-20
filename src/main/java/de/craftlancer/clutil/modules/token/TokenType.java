package de.craftlancer.clutil.modules.token;

public enum TokenType
{
    UNDEFINED("Undefined Token"),
    ENCHANTMENT("Enchantment Token");
    
    private String name;
    
    private TokenType(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public static TokenType getByName(String name)
    {
        for (TokenType type : values())
            if (type.getName().equalsIgnoreCase(name))
                return type;
        
        return null;
    }
}
