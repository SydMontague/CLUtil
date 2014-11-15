package de.craftlancer.clutil.modules.token;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import de.craftlancer.clutil.modules.CaptureTheToken;

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
    
    public TokenTracker(Plugin plugin, Location spawnLocation)
    {
        this.spawnLocation = spawnLocation;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public String getLocationString()
    {
        return "X " + getLocation().getBlockX() + " Z " + getLocation().getBlockZ();
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
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e)
    {
        ItemStack item = e.getPlayer().getItemInHand();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        e.setCancelled(true);
        
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventory(InventoryMoveItemEvent event)
    {
        ItemStack item = event.getItem();
        if (!TokenFactory.isToken(item) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        if (event.getDestination().getType() != InventoryType.PLAYER)
            event.setCancelled(true);
        
        setEntity((Entity) event.getDestination().getHolder());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventory(InventoryClickEvent event)
    {
        ItemStack item = null;
        
        if (event.isShiftClick())
            item = event.getCurrentItem();
        else
            item = event.getCursor();
        
        if (!TokenFactory.isToken(item) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        if (event.getInventory().getType() != InventoryType.CRAFTING && event.getRawSlot() < 27)
            event.setCancelled(true);
        
        setEntity(event.getWhoClicked());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(ItemSpawnEvent event)
    {
        ItemStack item = event.getEntity().getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        net.minecraft.server.v1_7_R4.Entity i = ((CraftItem) event.getEntity()).getHandle();
        try
        {
            Field f = net.minecraft.server.v1_7_R4.Entity.class.getDeclaredField("invulnerable");
            f.setAccessible(true);
            f.setBoolean(i, true);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
        {
            e.printStackTrace();
        }
        
        setEntity(event.getEntity());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickUp(PlayerPickupItemEvent event)
    {
        ItemStack item = event.getItem().getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        setEntity(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickUp(InventoryPickupItemEvent event)
    {
        ItemStack item = event.getItem().getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event)
    {
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
        ItemStack item = event.getEntity().getItemStack();
        if (!(TokenFactory.isToken(item)) || TokenFactory.getToken(item).getType() != TokenType.CAPTURE_EVENT)
            return;
        
        event.setCancelled(true);
    }
        
    @EventHandler
    public void onLogout(PlayerQuitEvent event)
    {
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
        if (event.getEntered().equals(getEntity()))
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event)
    {
        if (!event.getPlayer().equals(getEntity()))
            return;
        
        if (event.getPlayer().getLocation().distance(event.getTo()) < 8)
            return;
        
        event.setCancelled(true);
    }
    
    public void end()
    {
        HandlerList.unregisterAll(this);
        if (entity == null)
            return;
        if (entity.getType() == EntityType.DROPPED_ITEM)
            entity.remove();
        else if (entity instanceof Player)
            ((Player) entity).getInventory().removeItem(CaptureTheToken.TOKENITEM);
    }
    
}
