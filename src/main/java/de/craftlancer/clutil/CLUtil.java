package de.craftlancer.clutil;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import de.craftlancer.clutil.modules.AdvancedEnchantments;
import de.craftlancer.clutil.modules.EffectWeapons;
import de.craftlancer.clutil.modules.HeadHunter;
import de.craftlancer.clutil.modules.Home;
import de.craftlancer.clutil.modules.OreStones;
import de.craftlancer.clutil.modules.OwnHealth;
import de.craftlancer.clutil.modules.PumpkinBandit;
import de.craftlancer.clutil.modules.Speed;
import de.craftlancer.clutil.speed.BerserkSpeedModifier;
import de.craftlancer.clutil.speed.SneakSpeedModifier;
import de.craftlancer.clutil.speed.WaldSpeedModifier;
import de.craftlancer.speedapi.SpeedAPI;

public class CLUtil extends JavaPlugin
{
    private static CLUtil instance;
    private CommandStatsMe stats;
    private CommandSneak sneak;
    private CommandWaldlaeufer waldl;
    private GriefBlock gblock;
    private FileConfiguration config;
    public Logger log;
    
    private HashMap<ModuleType, Module> modules = new HashMap<>();
    
    @SuppressWarnings("deprecation")
    @Override
    public void onEnable()
    {
        instance = this;
        if (!new File(this.getDataFolder(), "config.yml").exists())
            saveDefaultConfig();
        
        stats = new CommandStatsMe(this);
        sneak = new CommandSneak(this);
        waldl = new CommandWaldlaeufer(this);
        gblock = new GriefBlock();
        gblock.runTaskTimer(this, 1200L, 1200L);
        config = getConfig();
        log = getLogger();
        
        getCommand("sneak").setExecutor(sneak);
        getCommand("statsme").setExecutor(stats);
        getCommand("find").setExecutor(new CommandFind(this));
        getCommand("clutil").setExecutor(new CommandCLUtil(this));
        getCommand("togglearrow").setExecutor(waldl);
        
        // new ResourceAlgoTest(this);
        // getServer().getPluginManager().registerEvents(new BuildingTest(), this);
        
        loadModules();
        
        getServer().getPluginManager().registerEvents(new UtilListener(this), this);
        getServer().getPluginManager().registerEvents(stats, this);
        getServer().getPluginManager().registerEvents(sneak, this);
        getServer().getPluginManager().registerEvents(waldl, this);
        getServer().getPluginManager().registerEvents(new AutoLevelUp(this), this);
        getServer().getPluginManager().registerEvents(new RollChange(), this);
        getServer().getPluginManager().registerEvents(gblock, this);
        getServer().getPluginManager().registerEvents(new FarmRebalance(), this);
        
        ItemStack arrow = new ItemStack(Material.ARROW, 2);
        ItemMeta meta = arrow.getItemMeta();
        meta.setDisplayName("§4PoisonArrow");
        arrow.setItemMeta(meta);
        
        getServer().addRecipe(new ShapedRecipe(arrow).shape("OF", "ES", "OC").setIngredient('F', Material.FLINT).setIngredient('E', Material.EXP_BOTTLE).setIngredient('S', Material.STICK).setIngredient('C', Material.FEATHER));
        
        ItemStack gravel = new ItemStack(Material.GRAVEL, 3);
        ItemMeta gmeta = gravel.getItemMeta();
        gmeta.setDisplayName("§4Griefblock");
        gravel.setItemMeta(gmeta);
        getServer().addRecipe(new ShapelessRecipe(gravel).addIngredient(3, Material.GRAVEL).addIngredient(Material.EXP_BOTTLE).addIngredient(Material.INK_SACK, 4));
        
    }
    
    private void loadModules()
    {
        for (ModuleType type : ModuleType.values())
        {
            if (isDeactivated(type))
                continue;
            
            modules.put(type, craftModule(type));
        }
    }
    
    private boolean isDeactivated(ModuleType type)
    {
        return config.getBoolean("modules." + type.name(), true);
    }
    
    private Module craftModule(ModuleType type)
    {
        switch (type)
        {
            case ADVENCHANTMENTS:
                return new AdvancedEnchantments(this);
            case EFFECTWEAPONS:
                return new EffectWeapons(this);
            case HEADHUNT:
                return new HeadHunter(this);
            case ORESTONES:
                return new OreStones(this);
            case OWNHEALTH:
                return new OwnHealth(this);
            case PUMPKINBANDIT:
                return new PumpkinBandit(this);
            case HOME:
                return new Home(this);
            case SPEED:
                return new Speed(this);
        }
        
        throw new IllegalArgumentException("Illegal ModuleType detected!");
    }
    
    @Override
    public void onDisable()
    {
        // BuildingManager.getInstance().save(true);
        
        for(Module mod : modules.values())
            mod.onDisable();
        
        stats.saveStats();
        config = null;
        getServer().getScheduler().cancelTasks(this);
        
        gblock.removeAllBlocks();
    }
    
    public static CLUtil getInstance()
    {
        return instance;
    }
    
    public static boolean isSneaking(Player p)
    {
        return isSneaking(p.getName());
    }
    
    public static boolean isSneaking(String name)
    {
        return getInstance().sneak.isSneaking(name);
    }
    
    public static boolean isUsingPoisonArrow(Player p)
    {
        return isUsingPoisonArrow(p.getName());
    }
    
    private static boolean isUsingPoisonArrow(String name)
    {
        return getInstance().waldl.isUsingPoisonArrow(name);
    }
    
    public static Location parseLocation(String loc)
    {
        if (loc == null)
            return null;
        
        String coords[] = loc.split(" ", 4);
        
        if (coords.length != 4)
            return null;
        
        for (int i = 0; i <= 2; i++)
            if (!coords[i].matches("-?[0-9]+"))
                return null;
        
        if (Bukkit.getServer().getWorld(coords[3]) == null)
            return null;
        
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        int z = Integer.parseInt(coords[2]);
        
        return new Location(Bukkit.getServer().getWorld(coords[3]), x, y, z);
    }
    
    public static String getLocationString(Location loc)
    {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " " + loc.getWorld().getName();
    }
    
    public void reload()
    {
        reloadConfig();
        config = null;
        config = getConfig();
    }
}
