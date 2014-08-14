package de.craftlancer.clutil.buildings.commands;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.buildings.BuildingManager;
import de.craftlancer.clutil.buildings.BuildingProcess;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.groups.GroupPlayer;
import de.craftlancer.groups.Town;
import de.craftlancer.groups.managers.PlayerManager;

public class BuildingProgressCommand extends SubCommand
{
    
    public BuildingProgressCommand(String permission, CLUtil plugin)
    {
        super(permission, plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        GroupPlayer gp = PlayerManager.getGroupPlayer(sender.getName());
        Town t = gp.getTown();
        if (t == null)
            return "You are in no Town"; // TODO externalise
        if (!t.hasPermission(sender.getName(), "town.build")) // TODO externalise
            return "You don't have the permission to place buildings."; // TODO externalise
            
        // Index | Name | [==========] 0% 0/6666
        // 1 | Gasthaus | [==========] 10% 667/6666
        
        for (Entry<Integer, BuildingProcess> entry : BuildingManager.getInstance().getProcesses().entrySet())
        {
            BuildingProcess process = entry.getValue();
            
            if (!process.getOwningTown().equals(t))
                continue;
            
            int blockSet = process.getBlocksSet();
            int blockTotal = process.getBuilding().getTotalBlocks();
            double ratio = (double) blockSet / blockTotal;
            
            StringBuilder message = new StringBuilder();
            message.append(entry.getKey()).append(" | ").append(process.getBuilding().getName()).append(" [").append(ChatColor.GREEN);
            for (int i = 0; i < ratio * 10; i++)
                message.append("=");
            message.append(ChatColor.WHITE);
            for (int i = 0; i < 10 - ratio * 10; i++)
                message.append("=");
            message.append("] ").append((int) (ratio * 100)).append("% ").append(blockSet).append("/").append(blockTotal);
            
            sender.sendMessage(message.toString());
        }
        
        return null;
    }
    
    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
