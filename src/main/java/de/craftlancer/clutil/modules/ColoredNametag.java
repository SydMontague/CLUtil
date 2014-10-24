package de.craftlancer.clutil.modules;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class ColoredNametag extends Module implements Listener
{
    
    public ColoredNametag(CLUtil plugin)
    {
        super(plugin);
        getPlugin().getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onNameTag(AsyncPlayerReceiveNameTagEvent e)
    {
        if (e.getTag().equals("PumpkinBandit"))
            return;
        
        try
        {
            ChatColor color = null;
            Resident namedResi = TownyUniverse.getDataSource().getResident(e.getNamedPlayer().getName());
            Resident seeingResi = TownyUniverse.getDataSource().getResident(e.getPlayer().getName());
            
            if (seeingResi.hasFriend(namedResi))
                color = ChatColor.GREEN;
            else if (!namedResi.hasNation() || !seeingResi.hasNation())
                return;
            else if (seeingResi.getTown().getNation().getAllies().contains(namedResi.getTown().getNation()))
                color = ChatColor.GREEN;
            else if (seeingResi.getTown().getNation().getEnemies().contains(namedResi.getTown().getNation()))
                color = ChatColor.RED;
            
            e.setTag(color + e.getTag());
        }
        catch (NotRegisteredException ex)
        {
            return;
        }
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.COLOREDNAMETAGS;
    }
    
}
