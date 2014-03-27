package de.craftlancer.clutil.buildings.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.craftlancer.clutil.buildings.Building;
import de.craftlancer.clutil.buildings.BuildingManager;
import de.craftlancer.core.CLPlugin;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.groups.GroupPlayer;
import de.craftlancer.groups.Town;
import de.craftlancer.groups.managers.PlayerManager;

public class BuildingPreviewCommand extends SubCommand
{
    
    public BuildingPreviewCommand(String permission, CLPlugin plugin)
    {
        super(permission, plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        if (args.length < 2 || !BuildingManager.getInstance().hasBuilding(args[1]))
            return "You need to specify a valid building!"; // TODO externalise
            
        GroupPlayer gp = PlayerManager.getGroupPlayer(sender.getName());
        Town t = gp.getTown();
        //TODO in town check
        if (t == null)
            return "You are in no Town"; // TODO externalise
            
        if (!t.hasPermission(sender.getName(), "town.build")) // TODO externalise
            return "You don't have the permission to place buildings."; // TODO externalise
            
        Building build = BuildingManager.getInstance().getBuilding(args[1]);
        build.createPreview((Player) sender);
        
        return "Zeige Vorschau des Gebäudes \"" + build.getName() + "\""; // TODO externalise
    }
    
    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
