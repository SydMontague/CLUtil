package de.craftlancer.clutil.buildings.commands;

import java.util.Map;

import org.bukkit.command.CommandSender;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.core.command.HelpCommand;
import de.craftlancer.core.command.SubCommand;

public class BuildingHelpCommand extends HelpCommand
{
    
    public BuildingHelpCommand(String permission, CLUtil plugin, Map<String, SubCommand> map)
    {
        super(permission, plugin, map);
    }

    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
