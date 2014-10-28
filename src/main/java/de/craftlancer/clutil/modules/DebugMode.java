package de.craftlancer.clutil.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.core.Utils;

public class DebugMode extends Module implements TabExecutor
{
    public static final String DEBUG_PERM = "cl.util.debug";
    
    private Map<String, Boolean> debugger = new HashMap<>();
    private boolean consoleLogging;
    
    public DebugMode(CLUtil plugin)
    {
        super(plugin);
        consoleLogging = getConfig().getBoolean("consoleLogging", false);
        
        if (getConfig().isConfigurationSection("default"))
            for (String key : getConfig().getConfigurationSection("default").getKeys(false))
                debugger.put(key, getConfig().getBoolean("default." + key, false));
        
        getPlugin().getCommand("debugger").setExecutor(this);
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.DEBUG;
    }
    
    public void debug(String debug, String message)
    {
        if (!debugger.containsKey(debug))
            debugger.put(debug, Boolean.FALSE);
        
        if (!debugger.get(debug))
            return;
        
        StringBuilder str = new StringBuilder("[").append(debug).append("] ").append(message);
        
        if (consoleLogging)
            getPlugin().getLogger().info(str.toString());
        
        for (Player p : Bukkit.getOnlinePlayers())
            if (p.hasPermission(DEBUG_PERM))
                p.sendMessage(str.toString());
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
            return false;
        
        String arg = args[0];
        
        if (debugger.containsKey(arg))
            debugger.put(arg, !debugger.get(arg));
        else
            debugger.put(arg, true);
        
        sender.sendMessage("Debugger " + arg + " set to " + debugger.get(arg));
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return Utils.getMatches(args[0], debugger.keySet());
            default:
                return null;
        }
    }
}
