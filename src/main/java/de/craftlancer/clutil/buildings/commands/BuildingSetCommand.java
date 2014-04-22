package de.craftlancer.clutil.buildings.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.craftlancer.clutil.buildings.Building;
import de.craftlancer.clutil.buildings.BuildingManager;
import de.craftlancer.core.CLPlugin;
import de.craftlancer.core.command.SubCommand;

public class BuildingSetCommand extends SubCommand
{
    
    public BuildingSetCommand(String permission, CLPlugin plugin)
    {
        super(permission, plugin, true);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        if (args.length <= 3)
            return "Not enough arguments!"; // TODO externalise
            
        if (!BuildingManager.getInstance().hasBuilding(args[1]))
            return "This building does not exist!"; // TODO externalise
            
        Building building = BuildingManager.getInstance().getBuilding(args[1]);
        
        switch (args[2])
        {
            case "buildable":
                boolean bool = Boolean.parseBoolean(args[3]);
                building.setBuildable(bool);
                break;
            case "desc":
                building.setDescription(args[3]);
                break;
            default:
                return "This key is unknown!";
        }
        
        return "Value successfully set.";
    }
    
    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
