package de.craftlancer.clutil.old;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@SuppressWarnings("deprecation")
public class ToolRestrictions implements Listener
{
    private double improper = 0.1;
    private double wood = 0.5;
    private double gold = 1.0;
    private double stone = 0.75;
    private double iron = 1.0;
    private double diamond = 1.0;
    
    private Set<Material> pickaxe = new HashSet<Material>()
    {
        private static final long serialVersionUID = -2772136037639218642L;
        {
            add(Material.COAL_ORE);
            add(Material.IRON_ORE);
            add(Material.REDSTONE_ORE);
            add(Material.GLOWING_REDSTONE_ORE);
            add(Material.GOLD_ORE);
            add(Material.DIAMOND_ORE);
            add(Material.LAPIS_ORE);
            add(Material.EMERALD_ORE);
            add(Material.STONE);
        }
    };
    
    private Set<Material> axe = new HashSet<Material>()
    {
        private static final long serialVersionUID = -2772136037639218642L;
        {
            add(Material.LOG);
        }
    };
    
    private Set<Material> hoe = new HashSet<Material>()
    {
        private static final long serialVersionUID = -2772136037639218642L;
        {
            add(Material.CROPS);
            add(Material.CARROT);
            add(Material.POTATO);
            add(Material.MELON_BLOCK);
            add(Material.MELON_STEM);
            add(Material.PUMPKIN);
            add(Material.PUMPKIN_STEM);
            add(Material.RED_MUSHROOM);
            add(Material.BROWN_MUSHROOM);
        }
    };
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e)
    {
        if (e.isCancelled())
            return;
        
        int item = e.getPlayer().getItemInHand().getTypeId();
        double chance = 1;
        boolean change = false;
        
        if (pickaxe.contains(e.getBlock().getType()))
        {
            switch (item)
            {
                case 257:
                    chance = iron;
                    break;
                case 270:
                    chance = wood;
                    break;
                case 274:
                    chance = stone;
                    break;
                case 278:
                    chance = diamond;
                    break;
                case 285:
                    chance = gold;
                    break;
                default:
                    chance = improper;
                    break;
            }
            change = true;
        }
        else if (axe.contains(e.getBlock().getType()))
        {
            switch (item)
            {
                case 271:
                    chance = wood;
                    break;
                case 275:
                    chance = stone;
                    break;
                case 258:
                    chance = iron;
                    break;
                case 279:
                    chance = diamond;
                    break;
                case 286:
                    chance = gold;
                    break;
                default:
                    chance = improper;
                    break;
            }
            change = true;
        }
        else if (hoe.contains(e.getBlock().getType()))
        {
            switch (item)
            {
                case 290:
                    chance = wood;
                    break;
                case 291:
                    chance = stone;
                    break;
                case 292:
                    chance = iron;
                    break;
                case 293:
                    chance = diamond;
                    break;
                case 294:
                    chance = gold;
                    break;
                default:
                    chance = improper;
                    break;
            }
            change = true;
        }
        
        if (change)
            if (Math.random() > chance)
                e.getBlock().setTypeId(0);
    }
}
