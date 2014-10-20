package de.craftlancer.clutil.old.buildings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class MassChestInventory implements Inventory
{
    private MassChestInventoryHolder holder;
    private String name;
    private String title;
    private final List<Inventory> inventories = new ArrayList<Inventory>();
    private int size = 0;
    
    public MassChestInventory(String name, String title)
    {
        this.holder = new MassChestInventoryHolder(this);
        this.name = name;
        this.title = title;
    }
    
    public MassChestInventory(String name, String title, Inventory... inventories)
    {
        this(name, title);
        for (Inventory i : inventories)
            addInventory(i);
    }
    
    public MassChestInventory(String name, String title, Collection<Inventory> inventories)
    {
        this(name, title);
        for (Inventory i : inventories)
            addInventory(i);
    }
    
    public boolean addInventory(Inventory i)
    {
        if (i.getType() != InventoryType.CHEST)
            return false;
        
        size += i.getSize();
        
        return inventories.add(i);
    }
    
    public boolean removeInventory(Inventory i)
    {
        boolean bool = i.getType() == InventoryType.CHEST && inventories.remove(i);
        
        if (bool)
            size -= i.getSize();
        
        return bool;
    }
    
    public List<Inventory> getInventories()
    {
        return inventories;
    }
    
    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException
    {
        Validate.noNullElements(items, "Item cannot be null");
        HashMap<Integer, ItemStack> leftOver = new HashMap<Integer, ItemStack>();
        
        int index = 0;
        
        for (ItemStack item : items)
        {
            item = item.clone();
            for (Inventory inventory : inventories)
            {
                if (item.getAmount() == 0)
                    break;
                
                Map<Integer, ItemStack> map = inventory.addItem(item);
                if (map.containsKey(0))
                    item = map.get(0);
                else
                    item.setAmount(0);
            }
            
            if (item.getAmount() != 0)
                leftOver.put(index, item);
            
            index++;
        }
        
        return leftOver;
    }
    
    /*
     * Taken from CraftInventory.java
     */
    @Override
    public HashMap<Integer, ? extends ItemStack> all(int materialId)
    {
        HashMap<Integer, ItemStack> slots = new HashMap<Integer, ItemStack>();
        
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack item = inventory[i];
            if (item != null && item.getTypeId() == materialId)
                slots.put(i, item);
        }
        return slots;
    }
    
    @Override
    public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException
    {
        HashMap<Integer, ItemStack> slots = new HashMap<Integer, ItemStack>();
        
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material)
                slots.put(i, item);
        }
        return slots;
    }
    
    /*
     * Taken from CraftInventory.java
     */
    @Override
    public HashMap<Integer, ? extends ItemStack> all(ItemStack item)
    {
        HashMap<Integer, ItemStack> slots = new HashMap<Integer, ItemStack>();
        if (item != null)
        {
            ItemStack[] inventory = getContents();
            for (int i = 0; i < inventory.length; i++)
                if (item.equals(inventory[i]))
                    slots.put(i, inventory[i]);
        }
        return slots;
    }
    
    @Override
    public void clear()
    {
        for (Inventory inventory : inventories)
            inventory.clear();
    }
    
    @Override
    public void clear(int slot)
    {
        setItem(slot, null);
    }
    
    @Override
    public boolean contains(int materialId)
    {
        for (Inventory inventory : inventories)
            if (inventory.contains(materialId))
                return true;
        
        return false;
    }
    
    @Override
    public boolean contains(Material material) throws IllegalArgumentException
    {
        for (Inventory inventory : inventories)
            if (inventory.contains(material))
                return true;
        
        return false;
    }
    
    @Override
    public boolean contains(ItemStack item)
    {
        for (Inventory inventory : inventories)
            if (inventory.contains(item))
                return true;
        
        return false;
    }
    
    @Override
    public boolean contains(int materialId, int amount)
    {
        for (Inventory inventory : inventories)
            if (inventory.contains(materialId, amount))
                return true;
        
        return false;
    }
    
    @Override
    public boolean contains(Material material, int amount) throws IllegalArgumentException
    {
        for (Inventory inventory : inventories)
            if (inventory.contains(material, amount))
                return true;
        
        return false;
    }
    
    @Override
    public boolean contains(ItemStack item, int amount)
    {
        for (Inventory inventory : inventories)
            if (inventory.contains(item, amount))
                return true;
        
        return false;
    }
    
    /*
     * Taken from CraftInventory.java
     */
    @Override
    public boolean containsAtLeast(ItemStack item, int amount)
    {
        if (item == null)
            return false;
        
        if (amount <= 0)
            return true;
        
        for (ItemStack i : getContents())
            if (item.isSimilar(i) && (amount -= i.getAmount()) <= 0)
                return true;
        
        return false;
    }
    
    /*
     * Taken from CraftInventory.java
     */
    @Override
    public int first(int materialId)
    {
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack item = inventory[i];
            if (item != null && item.getTypeId() == materialId)
                return i;
        }
        return -1;
    }
    
    @Override
    public int first(Material material) throws IllegalArgumentException
    {
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material)
                return i;
            
        }
        return -1;
    }
    
    @Override
    public int first(ItemStack arg)
    {
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack item = inventory[i];
            if (item != null && item.equals(arg))
                return i;
        }
        return -1;
    }
    
    /*
     * Taken from CraftInventory.java
     */
    @Override
    public int firstEmpty()
    {
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++)
            if (inventory[i] == null)
                return i;
        
        return -1;
    }
    
    @Override
    public ItemStack[] getContents()
    {
        ItemStack[] array = new ItemStack[getSize()];
        
        int index = 0;
        
        for (Inventory i : inventories)
            for (ItemStack item : i.getContents())
            {
                array[index] = item;
                index++;
            }
        
        return array;
    }
    
    @Override
    public InventoryHolder getHolder()
    {
        return holder;
    }
    
    @Override
    public ItemStack getItem(int slot)
    {
        for (Inventory inventory : inventories)
            if (inventory.getSize() > slot)
                return inventory.getItem(slot);
            else
                slot -= inventory.getSize();
        
        return null;
    }
    
    @Override
    public int getMaxStackSize()
    {
        return 64;
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public int getSize()
    {
        return size;
    }
    
    @Override
    public String getTitle()
    {
        return title;
    }
    
    @Override
    public InventoryType getType()
    {
        return InventoryType.CHEST;
    }
    
    @Override
    public List<HumanEntity> getViewers()
    {
        List<HumanEntity> viewer = new ArrayList<HumanEntity>();
        
        for (Inventory inventory : inventories)
            viewer.addAll(inventory.getViewers());
        
        return viewer;
    }
    
    @Override
    public ListIterator<ItemStack> iterator()
    {
        return new InventoryIterator(this);
    }
    
    @Override
    public ListIterator<ItemStack> iterator(int index)
    {
        if (index < 0)
            index += getSize() + 1;
        
        return new InventoryIterator(this, index);
    }
    
    @Override
    public void remove(int materialId)
    {
        for (Inventory i : inventories)
            i.remove(materialId);
    }
    
    @Override
    public void remove(Material material) throws IllegalArgumentException
    {
        for (Inventory i : inventories)
            i.remove(material);
    }
    
    @Override
    public void remove(ItemStack item)
    {
        for (Inventory i : inventories)
            i.remove(item);
    }
    
    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException
    {
        Validate.noNullElements(items, "Item cannot be null");
        HashMap<Integer, ItemStack> leftOver = new HashMap<Integer, ItemStack>();
        
        int index = 0;
        
        for (ItemStack item : items)
        {
            for (Inventory inventory : inventories)
            {
                if (item.getAmount() == 0)
                    break;
                
                Map<Integer, ItemStack> map = inventory.removeItem(item);
                if (map.containsKey(0))
                    item = map.get(0);
                else
                    item.setAmount(0);
            }
            
            if (item.getAmount() != 0)
                leftOver.put(index, item);
            
            index++;
        }
        
        return leftOver;
    }
    
    @Override
    public void setContents(ItemStack[] items) throws IllegalArgumentException
    {
        if (getSize() < items.length)
            throw new IllegalArgumentException("Invalid inventory size; expected " + getSize() + " or less");
        
        int remaining = items.length;
        ItemStack[] partArray;
        
        for (Inventory inventory : inventories)
        {
            if (remaining <= 0)
                break;
            
            partArray = new ItemStack[inventory.getSize()];
            System.arraycopy(items, getSize() - remaining, partArray, 0, Math.min(inventory.getSize(), remaining));
            inventory.setContents(partArray);
            
            remaining -= inventory.getSize();
        }
    }
    
    @Override
    public void setItem(int slot, ItemStack item)
    {
        for (Inventory inventory : inventories)
            if (inventory.getSize() > slot)
                inventory.setItem(slot, item);
            else
                slot -= inventory.getSize();
    }
    
    @Override
    public void setMaxStackSize(int arg0)
    {
        throw new UnsupportedOperationException("We do not permit changing the MaxStackSize yet.");
    }
    
}

/*
 * Taken from InventoryIterator.java (why the **** is the constructor in default scope?)
 */
class InventoryIterator implements ListIterator<ItemStack>
{
    private final Inventory inventory;
    private int nextIndex;
    private Boolean lastDirection; // true = forward, false = backward, null = haven't moved yet
    
    InventoryIterator(Inventory craftInventory)
    {
        this.inventory = craftInventory;
        this.nextIndex = 0;
    }
    
    InventoryIterator(Inventory craftInventory, int index)
    {
        this.inventory = craftInventory;
        this.nextIndex = index;
    }
    
    @Override
    public boolean hasNext()
    {
        return nextIndex < inventory.getSize();
    }
    
    @Override
    public ItemStack next()
    {
        lastDirection = true;
        return inventory.getItem(nextIndex++);
    }
    
    @Override
    public int nextIndex()
    {
        return nextIndex;
    }
    
    @Override
    public boolean hasPrevious()
    {
        return nextIndex > 0;
    }
    
    @Override
    public ItemStack previous()
    {
        lastDirection = false;
        return inventory.getItem(--nextIndex);
    }
    
    @Override
    public int previousIndex()
    {
        return nextIndex - 1;
    }
    
    @Override
    public void set(ItemStack item)
    {
        if (lastDirection == null)
        {
            throw new IllegalStateException("No current item!");
        }
        int i = lastDirection ? nextIndex - 1 : nextIndex;
        inventory.setItem(i, item);
    }
    
    @Override
    public void add(ItemStack item)
    {
        throw new UnsupportedOperationException("Can't change the size of an inventory!");
    }
    
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Can't change the size of an inventory!");
    }
}
