package de.craftlancer.clutil.buildings.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.craftlancer.clutil.buildings.Building;
import de.craftlancer.clutil.buildings.BuildingManager;
import de.craftlancer.core.CLPlugin;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;

public class BuildingInfoCommand extends SubCommand
{
    
    public BuildingInfoCommand(String permission, CLPlugin plugin)
    {
        super(permission, plugin, true);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
        if (args.length <= 1)
            return "You need to specify a building!"; // TODO externalise
            
        if (!BuildingManager.getInstance().hasBuilding(args[1]))
            return "This building does not exist!"; // TODO externalise
            
        /*
         * Name: Schmiede
         * Size: 10x10x10
         * Blocks: 666666
         * Feature:
         * Description:
         * Costs:
         */
        Building build = BuildingManager.getInstance().getBuilding(args[1]);
        
        long time = build.getNumBlocks() / BuildingManager.getInstance().getBlocksPerTick();
        time *= 20 / BuildingManager.getInstance().getDefaultPeriod();
        
        sender.sendMessage("Name: " + build.getName());
        sender.sendMessage("Größe: " + build.getSizeString());
        sender.sendMessage("Blöcke: " + build.getNumBlocks());
        sender.sendMessage("Feature: " + (build.getFeatureBuilding() != null ? build.getFeatureBuilding().getName() : "none"));
        sender.sendMessage("Beschreibung: " + build.getDescription());
        sender.sendMessage("Geschätze Bauzeit: " + Utils.getTimeString(time));
        sender.sendMessage("Kosten: UNIMPLEMENTED"); // TODO
        
        return null;
    }
    
    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
