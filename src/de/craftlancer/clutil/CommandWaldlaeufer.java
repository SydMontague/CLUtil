package de.craftlancer.clutil;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandWaldlaeufer implements Listener, CommandExecutor
{
    private Map<String, Boolean> usePoisonArrows = new HashMap<String, Boolean>();
    private CLUtil plugin;
    private ItemStack arrow;
    
    public CommandWaldlaeufer(CLUtil plugin)
    {
        this.plugin = plugin;
        arrow = new ItemStack(Material.ARROW, 1);
        
        // List<String> lore = new ArrayList<String>();
        // lore.add("PoisonArrow");
        ItemMeta meta = arrow.getItemMeta();
        meta.setDisplayName("§4PoisonArrow");
        // meta.setLore(lore);
        arrow.setItemMeta(meta);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args)
    {
        if (!(sender instanceof Player) || !sender.hasPermission("cl.util.arrow"))
            return false;
        
        toggleArrow((Player) sender);
        return true;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e)
    {
        if (!e.hasItem() || !(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) || !e.getPlayer().hasPermission("cl.util.arrow"))
            return;
        
        if (e.getItem().getType() == Material.ARROW && e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName() && e.getItem().getItemMeta().getDisplayName().equals("§4PoisonArrow"))
            toggleArrow(e.getPlayer());
    }
    
    private void toggleArrow(Player name)
    {
        boolean bool = usePoisonArrows.containsKey(name.getName()) ? usePoisonArrows.get(name.getName()) : false;
        usePoisonArrows.put(name.getName(), !bool);
        
        if (bool)
            name.sendMessage("Du hast Giftpfeile deaktiviert!");
        else
            name.sendMessage("Du hast Giftpfeile aktiviert!");
        
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArrowShot(ProjectileLaunchEvent e)
    {
        if (!e.getEntityType().equals(EntityType.ARROW) || !(e.getEntity().getShooter() instanceof Player))
            return;
        
        Player p = (Player) e.getEntity().getShooter();
        
        if (usePoisonArrows.containsKey(p.getName()) && usePoisonArrows.get(p.getName()) && p.getInventory().removeItem(arrow).isEmpty() && p.hasPermission("cl.util.arrow"))
        {
            p.getInventory().addItem(new ItemStack(Material.ARROW));
            e.getEntity().setMetadata("poisonArrow", new FixedMetadataValue(plugin, true));
        }
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
            e.setDamage(e.getDamage() * plugin.getConfig().getDouble("waldl_arrow_mod", 1.5D));
        
        if (e.getDamager().hasMetadata("poisonArrow"))
            ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, plugin.getConfig().getInt("waldl_duration", 10) * 20, plugin.getConfig().getInt("waldl_strenght", 0)));
    }

    public boolean isUsingPoisonArrow(String name)
    {
        if(usePoisonArrows.containsKey(name))
            return usePoisonArrows.get(name);
        return false;
    }
}
