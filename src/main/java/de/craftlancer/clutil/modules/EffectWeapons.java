package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

/*
 * craften
 * nutzen
 * 
 * ItemName
 * <Effekttyp> <Level> <Dauer>
 * 
 * Level: Zahlen
 * Dauer: (Minuten:Sekunden)
 * 
 * Effekttyp Alias
 * SPEED
 * SLOW
 * FAST_DIGGING
 * SLOW_DIGGING
 * INCREASE_DAMAGE
 * HEAL
 * HARM
 * JUMP
 * CONFUSION
 * REGENERATION
 * DAMAGE_RESISTANCE
 * FIRE_RESISTANCE
 * WATER_BREATHING
 * INVISIBILITY
 * BLINDNESS
 * NIGHT_VISION
 * HUNGER
 * WEAKNESS
 * POISON
 * WITHER
 * HEALTH_BOOST
 * ABSORPTION
 * SATURATION
 * 
 */
public class EffectWeapons extends Module implements Listener
{
    private static final int WEAPON_SLOT = 0;
    private static final int POTION_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    
    private List<PotionEffectType> allowedPots = new ArrayList<>();
    private int maxUses;
    private double durationModifier;
    
    private Map<UUID, ItemStack> selectedArrows = new HashMap<>();
    
    public EffectWeapons(CLUtil plugin)
    {
        super(plugin);
        for (String str : getConfig().getStringList("allowedPots"))
            if (PotionEffectType.getByName(str) != null)
                allowedPots.add(PotionEffectType.getByName(str));
        
        this.maxUses = getConfig().getInt("uses", 5);
        this.durationModifier = getConfig().getDouble("durationModifier", 0.1);
        getPlugin().getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private ItemStack getSelectedArrow(Player player)
    {
        return selectedArrows.get(player.getUniqueId()).clone();
    }
    
    private void setSelectedArrow(Player player, ItemStack item)
    {
        selectedArrows.put(player.getUniqueId(), item.clone());
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        
        Player player = (Player) event.getEntity();
        ItemStack first = player.getInventory().getItem(player.getInventory().first(Material.ARROW)).clone();
        ItemStack remove = first.clone();
        String lore;
        
        if (selectedArrows.containsKey(player.getUniqueId()))
        {
            ItemStack selected = getSelectedArrow(player);
            lore = selected.getItemMeta().getLore().get(0);
            remove = selected;
        }
        else if (first.hasItemMeta() && first.getItemMeta().hasLore())
            lore = first.getItemMeta().getLore().get(0);
        else
            return;
        
        remove.setAmount(1);
        first.setAmount(1);
        
        if (!player.getInventory().removeItem(remove).isEmpty())
            return;
        
        player.getInventory().addItem(first);
        player.updateInventory();
        
        event.getProjectile().setMetadata("clutil.poisonarrow", new FixedMetadataValue(getPlugin(), lore));
        
        if (!player.getInventory().contains(remove))
            player.sendMessage("Spezialpfeile Aufgebraucht!");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!event.hasItem() || !(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
            return;
        
        if (event.getItem().getType() != Material.ARROW || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasLore())
            return;
        
        if (getPotionEffect(event.getItem().getItemMeta().getLore().get(0)) == null)
            return;
        
        setSelectedArrow(event.getPlayer(), event.getItem());
        event.getPlayer().sendMessage("Arrow selected!");
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryInteract(InventoryClickEvent event)
    {
        if (event.getInventory().getType() == InventoryType.ANVIL)
            new AnvilUpdateTask((AnvilInventory) event.getInventory()).runTaskLater(getPlugin(), 1L);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        
        String lore = null;
        Entity damager = event.getDamager();
        
        if (damager.getType() == EntityType.ARROW && damager.hasMetadata("clutil.poisonarrow"))
            lore = event.getDamager().getMetadata("clutil.poisonarrow").get(0).asString();
        else if (damager.getType() == EntityType.PLAYER)
        {
            ItemStack item = ((HumanEntity) damager).getItemInHand();
            if (item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().size() >= 2)
            {
                ItemMeta meta = item.getItemMeta();
                lore = meta.getLore().get(0);
                int uses = getRemainingUses(meta.getLore().get(1));
                uses--;
                
                if (uses >= 1)
                {
                    ArrayList<String> tmp = new ArrayList<>();
                    tmp.add(meta.getLore().get(0));
                    tmp.add(getUsesString(uses));
                    meta.setLore(tmp);
                }
                else
                    meta.setLore(null);
                
                item.setItemMeta(meta);
            }
        }
        else
            return;
        
        PotionEffect effect = getPotionEffect(lore);
        
        if (effect == null)
            return;
        
        ((LivingEntity) event.getEntity()).addPotionEffect(effect, true);
    }
    
    private static String getUsesString(int uses)
    {
        return String.valueOf(uses);
    }
    
    private static int getRemainingUses(String string)
    {
        return Integer.parseInt(string);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickHighest(InventoryClickEvent e)
    {
        if (!(e.getInventory().getType() == InventoryType.ANVIL && e.getSlotType() == SlotType.RESULT && e.getCursor().getType() == Material.AIR))
            return;
        
        if (e.getInventory().getItem(POTION_SLOT).getType() != Material.POTION)
            return;
        
        e.getWhoClicked().setItemOnCursor(e.getInventory().getItem(RESULT_SLOT));
        e.getInventory().setItem(WEAPON_SLOT, null);
        e.getInventory().setItem(POTION_SLOT, null);
        e.getInventory().setItem(RESULT_SLOT, null);
    }
    
    class AnvilUpdateTask extends BukkitRunnable
    {
        private AnvilInventory inventory;
        
        public AnvilUpdateTask(AnvilInventory inventory)
        {
            this.inventory = inventory;
        }
        
        @Override
        public void run()
        {
            ItemStack i1 = inventory.getItem(WEAPON_SLOT);
            ItemStack i2 = inventory.getItem(POTION_SLOT);
            
            if (i1 == null || i2 == null)
                return;
            
            if (!isApplicableWeapon(i1) || !isApplicablePotion(i2))
                return;
            
            ItemStack result = i1.clone();
            ItemMeta meta = result.getItemMeta();
            if (i1.getType() == Material.ARROW)
                meta.setLore(Arrays.asList(getEffectLore(i2)));
            else
                meta.setLore(Arrays.asList(getEffectLore(i2), String.valueOf(getUses())));
            result.setItemMeta(meta);
            inventory.setItem(RESULT_SLOT, result);
        }
    }
    
    private PotionEffect getPotionEffect(String lore)
    {
        if (lore == null)
            return null;
        
        String[] split = lore.split(" ");
        
        if (split.length != 3)
            return null;
        
        PotionEffectType type = getPotionEffectType(split[0]);
        int duration;
        int amplifier;
        try
        {
            duration = parseDuration(split[2]);
            amplifier = parseAmplifier(split[1]);
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
        
        return new PotionEffect(type, duration, amplifier);
    }
    
    public int getUses()
    {
        return maxUses;
    }
    
    public String getEffectLore(ItemStack i2)
    {
        PotionEffect effect = Potion.fromItemStack(i2).getEffects().iterator().next();
        
        StringBuilder str = new StringBuilder();
        str.append(getPotionEffectString(effect.getType()));
        str.append(" ");
        str.append(effect.getAmplifier());
        str.append(" ");
        str.append(getDurationString(effect.getDuration()));
        
        return str.toString();
    }
    
    private static String getDurationString(int duration)
    {
        duration /= 20;
        int seconds = duration % 60;
        return "(" + duration / 60 + ":" + (seconds < 10 ? "0" : "") + seconds + ")";
    }
    
    private static String getPotionEffectString(PotionEffectType type)
    {
        return type.getName();
    }
    
    public boolean isApplicablePotion(ItemStack i2)
    {
        if (i2.getType() != Material.POTION)
            return false;
        
        Potion potion = Potion.fromItemStack(i2);
        
        if (potion.getEffects().isEmpty())
            return false;
        
        if (!allowedPots.contains(potion.getEffects().iterator().next().getType()))
            return false;
        
        return true;
    }
    
    public static boolean isApplicableWeapon(ItemStack item)
    {
        switch (item.getType())
        {
            case WOOD_SWORD:
            case STONE_SWORD:
            case GOLD_SWORD:
            case IRON_SWORD:
            case DIAMOND_SWORD:
            case ARROW:
                return true;
            default:
                return false;
        }
    }
    
    private static int parseAmplifier(String string)
    {
        return Integer.parseInt(string);
    }
    
    private int parseDuration(String string)
    {
        if (string.startsWith("(") && string.endsWith(")"))
        {
            String[] s = string.substring(1, string.length() - 1).split(":");
            if (s.length == 2)
            {
                int minutes = Integer.parseInt(s[0]);
                int seconds = Integer.parseInt(s[1]);
                
                return (int) ((minutes * 60 + seconds) * 20 * durationModifier);
            }
        }
        
        throw new NumberFormatException(string + "is not a valid time string!");
    }
    
    private static PotionEffectType getPotionEffectType(String input)
    {
        PotionEffectType type = PotionEffectType.getByName(input);
        if (type != null)
            return type;
        
        return null;
    }
    
    @Override
    public ModuleType getName()
    {
        return ModuleType.EFFECTWEAPONS;
    }
}
