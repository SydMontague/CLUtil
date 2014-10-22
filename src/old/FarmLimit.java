package de.craftlancer.clutil.old;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.ItemSpawnEvent;

import de.craftlancer.clutil.CLUtil;

public class FarmLimit implements Listener
{
    CLUtil plugin;
    
    public FarmLimit(CLUtil p)
    {
        this.plugin = p;
    }
    
    /*
     * Zu Listenen Events:
     * CreatureSpawnEvent
     * SheepRegrowEvent
     */
    
    @EventHandler
    public void onAnimalBreed(CreatureSpawnEvent e)
    {
        if (e.isCancelled())
            return;
        
        if (!e.getSpawnReason().equals(SpawnReason.BREEDING) && !e.getSpawnReason().equals(SpawnReason.EGG))
            return;
        
        e.setCancelled(checkEntity(e.getEntity()));
    }
    
    @EventHandler
    public void onAnimalKill(EntityDeathEvent e)
    {
        EntityType type = e.getEntityType();
        switch (type)
        {
            case PIG:
            case SHEEP:
            case CHICKEN:
            case MUSHROOM_COW:
            case COW:
                if (checkEntity(e.getEntity()))
                {
                    e.setDroppedExp(0);
                    e.getDrops().clear();
                }
                break;
            default:
                return;
        }
    }
    
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e)
    {
        if (e.getEntity().getItemStack().getType() != Material.EGG)
            return;
        
        for (Entity ent : e.getEntity().getNearbyEntities(1, 1, 1))
            if (ent.getType() == EntityType.CHICKEN)
            {
                if (checkEntity((LivingEntity) ent))
                    e.setCancelled(true);
                
                return;
            }
    }
    
    @SuppressWarnings("deprecation")
    private boolean checkEntity(LivingEntity e)
    {
        EntityType type = e.getType();
        int x = plugin.getConfig().getInt(type.getName() + ".X", 10);
        int y = plugin.getConfig().getInt(type.getName() + ".Y", 10);
        int z = plugin.getConfig().getInt(type.getName() + ".Z", 10);
        
        int countSame = 0;
        int countAll = 0;
        
        for (Entity ent : e.getNearbyEntities(x, y, z))
        {
            if (ent instanceof LivingEntity)
                countAll++;
            
            if (ent.getType().equals(type))
                countSame++;
        }
        
        return (countSame > plugin.getConfig().getInt(type.getName() + ".countSame", 10) || countAll > plugin.getConfig().getInt(type.getName() + ".countAll", 10));
        
    }
}
