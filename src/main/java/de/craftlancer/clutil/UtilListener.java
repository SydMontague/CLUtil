package de.craftlancer.clutil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.craftlancer.economy.CLEco;
import de.craftlancer.skilllevels.event.SkillLevelUpEvent;

public class UtilListener implements Listener
{
    private CLUtil plugin;
    private Set<String> startPerms = new HashSet<String>()
    {
        private static final long serialVersionUID = 1L;
        {
            add("-mcmmo.skills.swords");
            add("-mcmmo.ability.swords.bleed");
            add("-mcmmo.ability.swords.counterattack");
            add("-cl.util.armor.dia");
            add("-cl.util.armor.iron");
            add("-cl.util.berserk.speed");
            add("-cl.util.berserk.damage");
            add("-mcmmo.skills.archery");
            add("-mcmmo.ability.archery.bonusdamage");
            add("-mcmmo.ability.archery.daze");
            add("-mcmmo.ability.archery.trackarrows");
            add("-cl.util.find");
            add("-cl.util.arrow");
            add("-cl.util.wald.dmgmod");
            add("-cl.util.wald.speed");
            add("-mcmmo.skills.acrobatics");
            add("-mcmmo.ability.acrobatics.dodge");
            add("-mcmmo.ability.acrobatics.roll");
            add("-mcmmo.ability.acrobatics.gracefulroll");
            add("-shadow.hook");
            add("-shadow.airassassination");
            add("-shadow.advbackstab");
            add("-shadow.pickpocket");
            add("-shadow.lockpick");
            add("-cl.util.shear");
            add("-cl.util.sneak");
            add("-allowlockpicking.canpick");
        }
    };
    
    public UtilListener(CLUtil plugin)
    {
        this.plugin = plugin;
    }
    
    private HashMap<String, Long> map = new HashMap<String, Long>();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnderPearl(PlayerInteractEvent e)
    {
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().getType() == Material.ENDER_PEARL)
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonDeath(EntityDeathEvent e)
    {
        if (e.getEntity().hasMetadata("SkillLevels.ignore"))
        {
            e.setDroppedExp(0);
            return;
        }
        
        if (e.getEntityType() == EntityType.ENDER_DRAGON)
            e.setDroppedExp(2000);
    }
    
    // Potion rebalance start
    @EventHandler
    public void onTarnDamage(EntityDamageByEntityEvent e)
    {
        Player damager = null;
        
        if (e.getDamager() instanceof Player)
            damager = (Player) e.getDamager();
        else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player)
            damager = (Player) ((Projectile) e.getDamager()).getShooter();
        
        if ((e.getEntity() instanceof Player))
            for (PotionEffect ent : ((Player) e.getEntity()).getActivePotionEffects())
                if (ent.getType().equals(PotionEffectType.INVISIBILITY))
                    ((Player) e.getEntity()).removePotionEffect(ent.getType());
        
        if (damager != null)
            for (PotionEffect ent : damager.getActivePotionEffects())
                if (ent.getType().equals(PotionEffectType.INVISIBILITY))
                    damager.removePotionEffect(ent.getType());
    }
    
    @EventHandler
    public void onPotionSplash(PotionSplashEvent e)
    {
        for (PotionEffect ef : e.getPotion().getEffects())
            if (ef.getType().equals(PotionEffectType.POISON))
                for (LivingEntity ent : e.getAffectedEntities())
                    e.setIntensity(ent, e.getIntensity(ent) * 0.5);
    }
    
    @EventHandler
    public void onBrew(BrewEvent e)
    {
        if (e.getContents().getIngredient().getType() == Material.BLAZE_POWDER)
            e.setCancelled(true);
    }
    
    @EventHandler
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
    
    // Potion rebalance end
    
    // PvP rebalance start
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage2(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.PLAYER) || !(e.getEntity() instanceof LivingEntity))
            return;
        
        Player p = (Player) e.getDamager();
        ((LivingEntity) e.getEntity()).setNoDamageTicks(0);
        
        if (map.containsKey(p.getName()) && map.get(p.getName()) >= System.currentTimeMillis())
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage3(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.PLAYER) || !(e.getEntity() instanceof LivingEntity))
            return;
        
        Player p = (Player) e.getDamager();
        long time;
        
        if (p.getItemInHand().getType() == Material.SHEARS)
            time = plugin.getConfig().getLong("shortnodamage", 600L);
        else
            time = plugin.getConfig().getLong("nodamage", 1000L);
        
        map.put(p.getName(), System.currentTimeMillis() + time);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void damage(EntityDamageByEntityEvent e)
    {
        if (e.getDamager() instanceof Player && ((Player) e.getDamager()).getItemInHand().getType() == Material.SHEARS)
            if (((Player) e.getDamager()).hasPermission("cl.util.shear"))
                e.setDamage(e.getDamage() + 3.5);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void damageMonitor(EntityDamageByEntityEvent e)
    {
        if (!(e.getDamager() instanceof Player) || ((Player) e.getDamager()).getItemInHand().getType() != Material.SHEARS)
            return;
        
        short dura = (short) (((HumanEntity) e.getDamager()).getItemInHand().getDurability() + 1);
        ((HumanEntity) e.getDamager()).getItemInHand().setDurability(dura);
        
        if (dura > 238)
        {
            ((HumanEntity) e.getDamager()).setItemInHand(new ItemStack(Material.AIR));
            ((Player) e.getDamager()).playSound(e.getDamager().getLocation(), Sound.ITEM_BREAK, 0.5F, 0.5F);
        }
    }
    
    // PvP rebalance end
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVillagerInteract(PlayerInteractEntityEvent e)
    {
        if (e.getRightClicked().getType() == EntityType.VILLAGER)
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFirstJoin(PlayerJoinEvent e)
    {
        if (!e.getPlayer().hasPlayedBefore())
            for (String s : startPerms)
                PermissionsEx.getPermissionManager().getUser(e.getPlayer()).addPermission(s);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHealthPot(EntityRegainHealthEvent e)
    {
        if (e.getRegainReason() == RegainReason.MAGIC)
            e.setAmount(e.getAmount() * 2.5);
    }
    
    @EventHandler
    public void onLevelUp(SkillLevelUpEvent e)
    {
        if (plugin.getServer().getPlayer(e.getUser().getUUID()) == null)
            return;
        
        int gold = (e.getNewLevel() - e.getOldLevel()) * plugin.getConfig().getInt("goldPerLevel", 50);
        CLEco.getInstance().depositBalance(plugin.getServer().getPlayer(e.getUser().getUUID()).getInventory(), gold);
        
    }
    
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent e)
    {
        if (!e.getItem().getType().isEdible())
            return;
        
        float extra = 0;
        
        switch (e.getItem().getType())
        {
            case BREAD:
                extra = 2.4f;
                break;
            case CARROT_ITEM:
                extra = 4.8f;
                break;
            case BAKED_POTATO:
                extra = 7.2f;
                break;
            case POTATO_ITEM:
                extra = 0.6f;
                break;
            case POISONOUS_POTATO:
                extra = 1.2f;
                break;
            case GOLDEN_CARROT:
                extra = 14.4f;
                break;
            case PUMPKIN_PIE:
                extra = 4.8f;
                break;
            case COOKIE:
                extra = 0.4f;
                break;
            case MELON:
                extra = 1.2f;
                break;
            case MUSHROOM_SOUP:
                extra = 7.2f;
                break;
            case RAW_CHICKEN:
                extra = 1.2f;
                break;
            case COOKED_CHICKEN:
                extra = 7.2f;
                break;
            case RAW_BEEF:
                extra = 1.8f;
                break;
            case COOKED_BEEF:
                extra = 12.8f;
                break;
            case RAW_FISH:
                extra = 0.4f;
                break;
            case COOKED_FISH:
                extra = 9.6f;
                break;
            case PORK:
                extra = 1.8f;
                break;
            case GRILLED_PORK:
                extra = 12.8f;
                break;
            case APPLE:
                extra = 2.4f;
                break;
            case GOLDEN_APPLE:
                extra = 9.6f;
                break;
            case ROTTEN_FLESH:
                extra = 0.8f;
                break;
            case SPIDER_EYE:
                extra = 3.2f;
                break;
            default:
        }
        
        e.getPlayer().setSaturation(e.getPlayer().getSaturation() + extra);
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
        }.runTaskLater(plugin, 1);
    }
}
