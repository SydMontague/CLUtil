package de.craftlancer.clutil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandCLUtil implements CommandExecutor
{
    CLUtil plugin;
    
    public CommandCLUtil(CLUtil plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3)
    {
        if (!sender.hasPermission("cl.util.reload"))
            return false;
        
        plugin.reload();
        
        return true;
    }
    
}
