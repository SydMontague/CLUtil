package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.metadata.FixedMetadataValue;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

/*
 * eigenes Craftingrezept
 * 
 */
public class Enderball extends Module implements Listener
{
    private ItemStack item;
    private List<EntityType> types;
    
    public Enderball(CLUtil plugin)
    {
        super(plugin);
        types = new ArrayList<>();
        types.add(EntityType.COW);
        types.add(EntityType.PIG);
        types.add(EntityType.SHEEP);
        types.add(EntityType.CHICKEN);
        types.add(EntityType.SQUID);
        
        item = new ItemStack(Material.FIREBALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Enderball");
        item.setItemMeta(meta);
        
        Bukkit.addRecipe(new ShapelessRecipe(item).addIngredient(Material.ENDER_PEARL).addIngredient(Material.CHEST).addIngredient(Material.CHEST).addIngredient(Material.CHEST).addIngredient(Material.CHEST));
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!event.getPlayer().hasPermission("cl.util.enderball"))
            return;
        
        if (!item.isSimilar(event.getItem()))
            return;
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR)
            return;
        
        event.getPlayer().getInventory().removeItem(item);
        
        Snowball pro = event.getPlayer().launchProjectile(Snowball.class);
        pro.setMetadata("enderball", new FixedMetadataValue(getPlugin(), null));
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e)
    {
        if (!types.contains(e.getEntityType()))
            return;
        
        if (!e.getDamager().hasMetadata("enderball"))
            return;
        
        new SpawnEgg().setSpawnedType(e.getEntity().getType());
        ItemStack i = new ItemStack(Material.MONSTER_EGG);
        i.setData(new SpawnEgg(e.getEntity().getType()));
        i.setDurability((new SpawnEgg(e.getEntity().getType()).getData()));
        
        e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), i);
        e.getEntity().remove();
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.ENDERBALL;
    }
    
}
