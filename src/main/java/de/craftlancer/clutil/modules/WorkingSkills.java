package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.core.ValueWrapper;
import de.craftlancer.skilllevels.SkillLevels;

public class WorkingSkills extends Module implements Listener
{
    private List<WorkingSkill> skills = new ArrayList<>();
    private final Random rand = new Random();
    private SkillLevels slevel;
    
    public WorkingSkills(CLUtil plugin)
    {
        super(plugin);
        
        PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.getPlugin("SkillLevels") != null && pm.getPlugin("SkillLevels").isEnabled())
            slevel = (SkillLevels) pm.getPlugin("SkillLevels");
        else
            throw new UnknownDependencyException("Dependency 'SkillLevels' not found, but mandatory!");
        
        for (String key : getConfig().getKeys(false))
            skills.add(new WorkingSkill(getConfig().getConfigurationSection(key)));
    }
    
    public SkillLevels getSkillLevels()
    {
        return slevel;
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.WORKINGSKILLS;
    }
    
    /**
     * MONITOR - we do not change any outcome of the event, just drop more items IF the event was successful
     * ignoreCancelled - we don't want to double drop, if the event was cancelled
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleDoubledrop(BlockBreakEvent event)
    {
        Player p = event.getPlayer();
        Block b = event.getBlock();
        
        for (WorkingSkill skill : skills)
        {
            if (!p.hasPermission(skill.getPermission()))
                continue;
            
            if (!skill.isValidBlock(b))
                continue;
            
            if (rand.nextDouble() > skill.getDoubleChance(p))
                continue;
            
            for (ItemStack item : event.getBlock().getDrops(p.getItemInHand()))
                b.getWorld().dropItem(b.getLocation(), item);
        }
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void handleBlockPlace(BlockPlaceEvent event)
    {
        Block b = event.getBlock();
        
        for (WorkingSkill skill : skills)
        {
            if (!skill.isHandledBlockType(b))
                continue;
            
            b.setData(skill.getPlaceDataValue(b));
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDeplete(PlayerItemDamageEvent event)
    {
        ItemStack item = event.getDamagedItem();
        Player p = event.getPlayer();
        
        for(WorkingSkill skill : skills)
        {
            if(!p.hasPermission(skill.getPermission()))
                continue;
            
            if(!skill.isValidTool(item))
                continue;
            
            if(rand.nextDouble() > skill.getDuraChance(p))
                continue;
                
            event.setCancelled(true);
            return;
        }
    }
    
    class WorkingSkill
    {
        private static final byte IGNORE_DATA = -1;
        
        private Map<Material, Byte> doubleMats = new HashMap<>();
        private final ValueWrapper doubleChance;
        private List<Material> duraMats = new ArrayList<>();
        private final ValueWrapper duraChance;
        private final String permission;
        private final String levelSystem;
        
        public WorkingSkill(ConfigurationSection config)
        {
            permission = config.getString("permission");
            levelSystem = config.getString("levelSystem");
            doubleChance = new ValueWrapper(config.getString("doubleChance"));
            duraChance = new ValueWrapper(config.getString("duraChance"));
            
            ConfigurationSection mats = config.getConfigurationSection("doubleMats");
            for (String key : mats.getKeys(false))
                doubleMats.put(Material.matchMaterial(key), (byte) mats.getInt(key, IGNORE_DATA));
            
            for (String key : config.getStringList("duraMats"))
                duraMats.add(Material.matchMaterial(key));
        }
        
        public byte getPlaceDataValue(Block b)
        {
            return doubleMats.get(b.getType());
        }
        
        public double getDoubleChance(Player p)
        {
            int level = getSkillLevels().getLevelSystem(levelSystem).getUser(p).getLevel();
            return doubleChance.getValue(level);
        }
        
        public double getDuraChance(Player p)
        {
            int level = getSkillLevels().getLevelSystem(levelSystem).getUser(p).getLevel();
            return duraChance.getValue(level);
        }
        
        public boolean isValidTool(ItemStack item)
        {
            return duraMats.contains(item.getType());
        }
        
        public String getPermission()
        {
            return permission;
        }
        
        @SuppressWarnings("deprecation")
        public boolean isValidBlock(Block b)
        {
            if (!doubleMats.containsKey(b.getType()))
                return false;
            
            byte value = doubleMats.get(b.getType());
            
            if (value != IGNORE_DATA && doubleMats.get(b.getType()) == b.getData())
                return false;
            
            return true;
        }
        
        public boolean isHandledBlockType(Block b)
        {
            return doubleMats.containsKey(b.getType()) && doubleMats.get(b.getType()) != IGNORE_DATA;
        }
    }
}
