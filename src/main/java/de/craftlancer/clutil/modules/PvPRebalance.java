package de.craftlancer.clutil.modules;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class PvPRebalance extends Module implements Listener
{
    private long noDamage = 1000L;
    private HashMap<UUID, Long> map = new HashMap<UUID, Long>();
    
    public PvPRebalance(CLUtil plugin)
    {
        super(plugin);
        noDamage = getConfig().getLong("noDamage", 1000L);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamageLowest(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.PLAYER) || !(e.getEntity() instanceof LivingEntity))
            return;
        
        Player damager = (Player) e.getDamager();
        ((LivingEntity) e.getEntity()).setNoDamageTicks(0);
        
        if (map.containsKey(damager.getUniqueId()) && map.get(damager.getUniqueId()) >= System.currentTimeMillis())
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageMonitor(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.PLAYER))
            return;
        
        map.put(((Player) e.getDamager()).getUniqueId(), System.currentTimeMillis() + noDamage);
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.PVPREBALANCE;
    }
}
