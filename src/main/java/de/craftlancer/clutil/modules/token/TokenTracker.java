package de.craftlancer.clutil.modules.token;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;

/*
 * update entity when item is:
 * taken out of chest //
 * dropped //
 * picked up //
 * 
 * prevent token
 * being placed in other inventories than player inventories //
 * being destroyed by any cause (especially Despawn, Lava and Fire) //
 * being crafted into something //
 * being picked up by anything else, than players //
 * being taken out of the game by logging out //
 * 
 * add persistent slowness effect to entity //
 * prevent entity from using any teleport/fasttravel things //
 */
public class TokenTracker implements Listener
{
    private Entity entity;
    private final Location spawnLocation;
    private boolean ended;
    
    public TokenTracker(Location spawnLocation)
    {
        this.spawnLocation = spawnLocation;
    }
    
    public String getLocationString()
    {
        return "X " + getLocation().getBlockX() + " Y " + getLocation().getBlockY();
    }
    
    public Location getLocation()
    {
        return getEntity() == null ? spawnLocation : getEntity().getLocation();
    }
    
    private void setEntity(Entity entity)
    {
        this.entity = entity;
    }
    
    public Entity getEntity()
    {
        return entity;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventory(InventoryMoveItemEvent event)
    {
        if (ended)
            return;
        
        ItemStack item = event.getItem();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        if (event.getDestination().getType() != InventoryType.PLAYER)
            event.setCancelled(true);
        
        setEntity((Entity) event.getDestination().getHolder());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(ItemSpawnEvent event)
    {
        if (ended)
            return;
        
        ItemStack item = event.getEntity().getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        setEntity(event.getEntity());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickUp(PlayerPickupItemEvent event)
    {
        if (ended)
            return;
        
        ItemStack item = event.getItem().getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        setEntity(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickUp(InventoryPickupItemEvent event)
    {
        if (ended)
            return;
        
        ItemStack item = event.getItem().getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event)
    {
        if (ended)
            return;
        
        for (ItemStack item : event.getInventory())
        {
            if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
                continue;
            
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event)
    {
        if (ended)
            return;
        
        ItemStack item = event.getEntity().getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event)
    {
        if (ended)
            return;
        
        if (event.getEntityType() != EntityType.DROPPED_ITEM)
            return;
        
        ItemStack item = ((Item) event.getEntity()).getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onDamage(EntityDeathEvent event)
    {
        if (ended)
            return;
        
        if (event.getEntityType() != EntityType.DROPPED_ITEM)
            return;
        
        ItemStack item = ((Item) event.getEntity()).getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        setEntity(event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), item));
    }
    
    @EventHandler
    public void onLogout(PlayerQuitEvent event)
    {
        if (ended)
            return;
        
        Player player = event.getPlayer();
        
        for (ItemStack item : player.getInventory())
        {
            if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
                continue;
            
            player.getInventory().remove(item);
            setEntity(player.getWorld().dropItem(player.getLocation(), item));
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event)
    {
        if (ended)
            return;
        
        if (event.getEntered().equals(getEntity()))
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event)
    {
        if (ended)
            return;
        
        if (!event.getPlayer().equals(getEntity()))
            return;
        
        if (event.getPlayer().getLocation().distance(event.getTo()) < 8)
            return;
        
        event.setCancelled(true);
    }
    
    public void end()
    {
        ended = true;
    }
    
}
