package de.craftlancer.clutil.modules;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.core.ValueWrapper;

public class DistanceShot extends Module implements Listener
{
    private int minDistance;
    private int maxDistance;
    private ValueWrapper damage;
    
    public DistanceShot(CLUtil plugin)
    {
        super(plugin);
        getConfig().getDouble("damageMod", 1.5);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent event)
    {
        if(event.getEntityType() != EntityType.PLAYER)
            return;
        
        if(!((Player) event.getEntity()).hasPermission("cl.util.precision"))
            return;
        
        event.getProjectile().setMetadata("cl.util.precision", new FixedMetadataValue(getPlugin(), event.getEntity().getLocation()));
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHeadshot(EntityDamageByEntityEvent event)
    {
        if(!event.getDamager().hasMetadata("cl.util.precision"))
            return;
        
        Location start = (Location) event.getDamager().getMetadata("cl.util.precision").get(0).value();
        double distance = start.distance(event.getDamager().getLocation()) - minDistance;
        
        if(distance <= 0)
            return;
        
        if(distance > maxDistance)
            distance = maxDistance;
        
        event.setDamage(DamageModifier.BASE, event.getDamage(DamageModifier.BASE) * 1 + damage.getValue((int) distance));
    }
    
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.DISTANCESHOT;
    }
    
}
