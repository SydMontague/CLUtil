package de.craftlancer.clutil.modules;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class PotionRebalance extends Module implements Listener
{
    private double POISON_MODIFIER = 0.5;
    private double HEALTH_MODIFIER = 2.5;
    private long rougeInvisDelay = 120;
    
    public PotionRebalance(CLUtil plugin)
    {
        super(plugin);
        POISON_MODIFIER = getConfig().getDouble("poison_modifier", 0.5);
        HEALTH_MODIFIER = getConfig().getDouble("health_modifier", 2.5);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.POTIONREBALANCE;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTarnDamage(EntityDamageByEntityEvent event)
    {
        final LivingEntity damager;
        
        if (event.getDamager() instanceof LivingEntity)
            damager = (LivingEntity) event.getDamager();
        else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity)
            damager = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
        else
            damager = null;
        
        if ((event.getEntity() instanceof LivingEntity))
            for (PotionEffect ent : ((LivingEntity) event.getEntity()).getActivePotionEffects())
                if (ent.getType().equals(PotionEffectType.INVISIBILITY))
                    ((LivingEntity) event.getEntity()).removePotionEffect(ent.getType());
        
        if (damager != null)
            for (final PotionEffect ent : damager.getActivePotionEffects())
                if (ent.getType().equals(PotionEffectType.INVISIBILITY))
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            damager.removePotionEffect(ent.getType());
                        }
                    }.runTaskLater(getPlugin(), getRemoveDelay(damager));
    }
    
    private long getRemoveDelay(LivingEntity damager)
    {
        if (!(damager instanceof Player))
            return 0;
        
        if (((Player) damager).hasPermission("cl.util.rogue"))
            return rougeInvisDelay;
        
        return 0;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event)
    {
        for (PotionEffect ef : event.getPotion().getEffects())
            if (ef.getType().equals(PotionEffectType.POISON))
                for (LivingEntity ent : event.getAffectedEntities())
                    event.setIntensity(ent, event.getIntensity(ent) * POISON_MODIFIER);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBrew(BrewEvent e)
    {
        if (e.getContents().getIngredient().getType() == Material.BLAZE_POWDER)
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent e)
    {
        if (!(e.getTarget() instanceof Player))
            return;
        
        for (PotionEffect eff : ((Player) e.getTarget()).getActivePotionEffects())
            if (eff.getType().equals(PotionEffectType.INVISIBILITY))
            {
                e.setCancelled(true);
                return;
            }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHealthPot(EntityRegainHealthEvent e)
    {
        if (e.getRegainReason() == RegainReason.MAGIC)
            e.setAmount(e.getAmount() * HEALTH_MODIFIER);
    }
}
