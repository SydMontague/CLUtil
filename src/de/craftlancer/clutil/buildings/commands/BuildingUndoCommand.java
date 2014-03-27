package de.craftlancer.clutil.buildings.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.craftlancer.clutil.buildings.BuildingManager;
import de.craftlancer.core.CLPlugin;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.groups.GroupPlayer;
import de.craftlancer.groups.Town;
import de.craftlancer.groups.managers.PlayerManager;

public class BuildingUndoCommand extends SubCommand
{
    
    public BuildingUndoCommand(String permission, CLPlugin plugin)
    {
        super(permission, plugin, false);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        if (args.length < 2)
            return "You need to specify an index!";
        
        GroupPlayer gp = PlayerManager.getGroupPlayer(sender.getName());
        Town t = gp.getTown();
        
        if (t == null)
            return "You are in no Town"; // TODO externalise
        if (!t.hasPermission(sender.getName(), "town.build")) // TODO externalise
            return "You don't have the permission to place buildings."; // TODO externalise
            
        int index = 0;
        
        try
        {
            index = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            return "You need to specify an index!";// TODO externalise
        }
        
        if (BuildingManager.getInstance().undoProcess(index))
            return "There is no process with the specified index!"; // TODO externalise
            
        return "Undoed building process!"; // TODO externalise
    }
    
    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
