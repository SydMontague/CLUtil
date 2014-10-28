package de.craftlancer.clutil.modules;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class PvPRebalance extends Module implements Listener
{
    private long noDamage = 1000L;
    private final double waldDamageMod;
    private HashMap<UUID, Long> map = new HashMap<UUID, Long>();
    
    public PvPRebalance(CLUtil plugin)
    {
        super(plugin);
        waldDamageMod = getConfig().getDouble("waldDamageMod", 1.5D);
        noDamage = getConfig().getLong("noDamage", 1000L);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
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
    
    @EventHandler
    public void onRespawn(final PlayerRespawnEvent e)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 10));
            }
        }.runTaskLater(getPlugin(), 1);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.ARROW) || !(e.getEntity() instanceof LivingEntity))
            return;
        
        if (!(((Arrow) e.getDamager()).getShooter() instanceof Player))
            return;
        
        Player p = (Player) ((Arrow) e.getDamager()).getShooter();
        
        if (p.hasPermission("cl.util.wald.dmgmod"))
            e.setDamage(e.getDamage() * waldDamageMod);
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.PVPREBALANCE;
    }
}
