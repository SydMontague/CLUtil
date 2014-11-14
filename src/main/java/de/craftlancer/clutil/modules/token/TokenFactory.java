package de.craftlancer.clutil.modules.token;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * 
 * 
 * 
 */
public class TokenFactory
{
    public static Material getTokenMaterial()
    {
        return Material.NETHER_STAR;
    }
    
    public static Token getToken(ItemStack i2)
    {
        TokenType type = TokenType.getByName(i2.getItemMeta().getDisplayName());
        
        if (type == null)
            return null;
        
        switch (type)
        {
            case ENCHANTMENT:
            {
                String lore = i2.getItemMeta().getLore().get(0);
                Enchantment ench = Enchantment.getByName(lore);
                
                return new EnchantmentToken(ench);
            }
            case UNDEFINED:
                return new UndefinedToken();
            case CAPTURE_EVENT:
                return new CaptureEventToken();
            default:
                return null;
        }
    }
    
    public static boolean isToken(ItemStack i2)
    {
        if (i2.getType() != getTokenMaterial() && i2.getType() != getTokenMaterial2())
            return false;
        
        if (!i2.getItemMeta().hasDisplayName())
            return false;
        
        TokenType type = TokenType.getByName(i2.getItemMeta().getDisplayName());
        
        if (type == null)
            return false;
        
        switch (type)
        {
            case ENCHANTMENT:
                if (!i2.getItemMeta().hasLore())
                    return false;
                if (Enchantment.getByName(i2.getItemMeta().getLore().get(0)) == null)
                    return false;
                return true;
            case UNDEFINED:
                return true;
            case CAPTURE_EVENT:
                return true;
            default:
                return false;
        }
    }
    
    private static Material getTokenMaterial2()
    {
        return Material.SPONGE;
    }

    public static ItemStack craftEnchantmentTokenItem(Enchantment entry)
    {
        ItemStack item = new ItemStack(getTokenMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TokenType.ENCHANTMENT.getName());
        meta.setLore(Arrays.asList(entry.getName()));
        item.setItemMeta(meta);
        
        return item;
    }
    
}
