package de.craftlancer.clutil.old.buildings.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.old.buildings.Building;
import de.craftlancer.clutil.old.buildings.BuildingManager;
import de.craftlancer.core.command.SubCommand;

public class BuildingListCommand extends SubCommand
{
    // TODO externalise
    private static int ITEMS_PER_PAGE = 5;
    
    public BuildingListCommand(String permission, CLUtil plugin)
    {
        super(permission, plugin, true);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
        
        int page = 1;
        String category = null;
        
        switch (args.length)
        {
            case 1:
                page = 1;
                break;
            case 2:
                try
                {
                    page = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e)
                {
                    category = args[1];
                }
                break;
            default:
                page = Integer.parseInt(args[1]);
                category = args[2];
                break;
        }
        
        page--;
        
        if (page < 0)
            page = 0;
        
        List<Building> buildings;
        
        if (category != null)
        {
            buildings = new ArrayList<Building>();
            
            for (Building b : BuildingManager.getInstance().getBuildings())
                if (b.hasCategory(category))
                    buildings.add(b);
        }
        else
            buildings = BuildingManager.getInstance().getBuildings();
        
        for (int i = 0; i < ITEMS_PER_PAGE; i++)
        {
            if (buildings.size() <= page * ITEMS_PER_PAGE + i)
                break;
            
            // Name || # Blocks || x*y*z || Feature
            // Schmiede | Blocks: 55555 | Size: 10x10x10 | Feature: Schmiede
            
            Building build = buildings.get(page * ITEMS_PER_PAGE + i);
            
            String message = build.getName() + " | Blöcke: " + build.getNumBlocks() + " | Größe: " + build.getSizeString();
            if (build.getFeatureBuilding() != null)
                message += " | Feature: " + build.getFeatureBuilding().getName();
            
            sender.sendMessage(message);
        }
        
        return null;
    }
    
    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
