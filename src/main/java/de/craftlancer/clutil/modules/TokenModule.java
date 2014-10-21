package de.craftlancer.clutil.modules;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class TokenModule extends Module
{
    public TokenModule(CLUtil plugin)
    {
        super(plugin);
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.TOKEN;
    }
    
}
