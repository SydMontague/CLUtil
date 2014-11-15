package de.craftlancer.clutil.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.clutil.modules.token.TokenFactory;

public class TokenModule extends Module implements Listener
{
    public TokenModule(CLUtil plugin)
    {
        super(plugin);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.TOKEN;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event)
    {
        for (ItemStack item : event.getInventory())
        {
            if (!(TokenFactory.isToken(item)))
                continue;
            
            event.setCancelled(true);
            return;
        }
    }
}
