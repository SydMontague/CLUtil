package de.craftlancer.clutil;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandFind implements CommandExecutor
{
    CLUtil plugin;
    
    public CommandFind(CLUtil plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!sender.hasPermission("cl.util.find") || !(sender instanceof Player))
            return true;
        
        Player p = (Player) sender;
        
        if (args.length < 1 || plugin.getServer().getPlayer(args[0]) == null)
            sender.sendMessage("Der Angegebene Spieler existiert nicht bzw. ist nicht online!");
        else if (!p.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 3))
            sender.sendMessage("Du musst 3 Eisenbarren im Inventar haben, um diesen Skill zu nutzen!");
        else
        {
            Player player = plugin.getServer().getPlayer(args[0]);
            p.setCompassTarget(player.getLocation());
            p.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, 3));
            p.sendMessage("Kompass auf " + player.getName() + " ausgerichtet!");
        }
        
        return true;
    }
    
}
