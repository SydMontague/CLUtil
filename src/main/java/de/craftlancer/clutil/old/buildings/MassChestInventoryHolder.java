package de.craftlancer.clutil.old.buildings;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MassChestInventoryHolder implements InventoryHolder
{
    // TODO extend to give class more point (maybe handle inventories/chests here instead of the inventory itself
    private MassChestInventory inventory;
    
    public MassChestInventoryHolder(MassChestInventory inventory)
    {
        this.inventory = inventory;
    }
    
    @Override
    public Inventory getInventory()
    {
        return inventory;
    }
    
}
