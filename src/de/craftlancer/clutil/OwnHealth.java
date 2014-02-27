package de.craftlancer.clutil;

import java.util.HashMap;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class OwnHealth implements Listener
{
    private HashMap<EntityType, Integer> mobs = new HashMap<EntityType, Integer>()
    {
        private static final long serialVersionUID = -4321713141306242628L;
        {
            put(EntityType.PLAYER, 50);
//            put(EntityType.ZOMBIE, 40);
//            put(EntityType.SKELETON, 40);
//            put(EntityType.CREEPER, 40);
//            put(EntityType.SPIDER, 40);
//            put(EntityType.CAVE_SPIDER, 35);
//            put(EntityType.ENDERMAN, 100);
//            put(EntityType.PIG_ZOMBIE, 50);
//            put(EntityType.GHAST, 25);
//            put(EntityType.BLAZE, 50);
//            put(EntityType.ENDER_DRAGON, 600);
//            put(EntityType.COW, 20);
//            put(EntityType.PIG, 20);
//            put(EntityType.CHICKEN, 8);
//            put(EntityType.SQUID, 20);
//            put(EntityType.VILLAGER, 50);
//            put(EntityType.SNOWMAN, 36);
//            put(EntityType.IRON_GOLEM, 200);
//            put(EntityType.SILVERFISH, 16);
//            put(EntityType.MUSHROOM_COW, 20);
//            put(EntityType.SHEEP, 16);
//            put(EntityType.WITCH, 65);
//            put(EntityType.WITHER, 900);
        }
    };
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if (!e.getPlayer().hasPlayedBefore())
        {
            e.getPlayer().setMaxHealth(mobs.get(EntityType.PLAYER));
            e.getPlayer().setHealth(e.getPlayer().getMaxHealth());
        }
        e.getPlayer().setHealthScale(20);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e)
    {
        LivingEntity ent = e.getEntity();
        switch (e.getEntityType())
        {
            case MAGMA_CUBE:
            case SLIME:
                ent.setMaxHealth((int) (Math.pow(((Slime) ent).getSize(), 2) * 3));
                break;
            case WOLF:
            case OCELOT:
                Tameable pet = (Tameable) ent;
                if (pet.isTamed())
                    ent.setMaxHealth(40);
                else
                    ent.setMaxHealth(20);
                break;
            default:
                if (mobs.containsKey(e.getEntityType()))
                    ent.setMaxHealth(mobs.get(e.getEntityType()));
                else
                    return;
        }
        
        ent.setHealth(ent.getMaxHealth());
    }
}
