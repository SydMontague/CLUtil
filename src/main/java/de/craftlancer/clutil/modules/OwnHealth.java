package de.craftlancer.clutil.modules;

import java.util.HashMap;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.core.ValueWrapper;

public class OwnHealth extends Module implements Listener
{
    private HashMap<EntityType, ValueWrapper> mobs = new HashMap<EntityType, ValueWrapper>();
    
    @SuppressWarnings("deprecation")
    public OwnHealth(CLUtil plugin)
    {
        super(plugin);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        
        for (String key : getConfig().getKeys(false))
        {
            if (EntityType.fromName(key) == null)
                continue;
            
            mobs.put(EntityType.fromName(key), new ValueWrapper(getConfig().getString(key)));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        
        if (!player.hasPlayedBefore() || player.getMaxHealth() != mobs.get(EntityType.PLAYER).getValue(0))
        {
            player.setMaxHealth(mobs.get(EntityType.PLAYER).getValue(0));
            player.setHealth(player.getMaxHealth());
        }
        player.setHealthScale(20);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e)
    {
        LivingEntity ent = e.getEntity();
        EntityType type = e.getEntityType();
        
        if (!mobs.containsKey(e.getEntityType()))
            return;
        
        switch (type)
        {
            case PLAYER:
                return;
            case MAGMA_CUBE:
            case SLIME:
                ent.setMaxHealth(mobs.get(type).getValue(((Slime) ent).getSize()));
                break;
            case WOLF:
            case OCELOT:
                Tameable pet = (Tameable) ent;
                ent.setMaxHealth(mobs.get(type).getValue(pet.isTamed() ? 1 : 0));
                break;
            default:
                ent.setMaxHealth(mobs.get(e.getEntityType()).getValue(0));
        }
        
        ent.setHealth(ent.getMaxHealth());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTame(EntityTameEvent event)
    {
        EntityType type = event.getEntityType();
        switch (type)
        {
            case WOLF:
            case OCELOT:
                event.getEntity().setMaxHealth(mobs.get(type).getValue(1));
                break;
            default:
                return;
        }
    }
    
    @Override
    public ModuleType getName()
    {
        return ModuleType.OWNHEALTH;
    }
}
