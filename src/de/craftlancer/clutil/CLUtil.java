package de.craftlancer.clutil;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

import de.craftlancer.clutil.speed.ArmorSpeedModifier;
import de.craftlancer.clutil.speed.BerserkSpeedModifier;
import de.craftlancer.clutil.speed.WaldSpeedModifier;
import de.craftlancer.speedapi.SpeedAPI;

//TODO enderpearl after death fix
public class CLUtil extends JavaPlugin
{
    private static CLUtil instance;
    private CommandStatsMe stats;
    private CommandSneak sneak;
    private CommandWaldlaeufer waldl;
    private CommandHome home;
    private GriefBlock gblock;
    private FileConfiguration config;
    public Logger log;
    public Set<Location> expsigns = new HashSet<Location>();
    public HashMap<Material, HashMap<Material, Double>> oreStones;
    
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
        home = new CommandHome(this);
        gblock = new GriefBlock();
        gblock.runTaskTimer(this, 1200L, 1200L);
        config = getConfig();
        log = getLogger();
        
        loadExpSigns();
        loadOreStones();
        getCommand("sneak").setExecutor(sneak);
        getCommand("statsme").setExecutor(stats);
        getCommand("find").setExecutor(new CommandFind(this));
        getCommand("clutil").setExecutor(new CommandCLUtil(this));
        getCommand("togglearrow").setExecutor(waldl);
        getCommand("home").setExecutor(home);
        
        // new ResourceAlgoTest(this);
        getServer().getPluginManager().registerEvents(new BuildingTest(), this);
        
        getServer().getPluginManager().registerEvents(new UtilListener(this), this);
        getServer().getPluginManager().registerEvents(new PumpkinBandit(), this);
        getServer().getPluginManager().registerEvents(new OreStones(this), this);
        getServer().getPluginManager().registerEvents(new OwnHealth(), this);
        getServer().getPluginManager().registerEvents(stats, this);
        getServer().getPluginManager().registerEvents(sneak, this);
        getServer().getPluginManager().registerEvents(waldl, this);
        getServer().getPluginManager().registerEvents(new AutoLevelUp(this), this);
        getServer().getPluginManager().registerEvents(new RollChange(), this);
        getServer().getPluginManager().registerEvents(new HeadHunter(), this);
        getServer().getPluginManager().registerEvents(new AdvancedEnchantments(this), this);
        getServer().getPluginManager().registerEvents(gblock, this);
        getServer().getPluginManager().registerEvents(home, this);
        getServer().getPluginManager().registerEvents(new FarmRebalance(), this);
        
        home.runTaskTimer(this, 10L, 10L);
        
        BerserkSpeedModifier berserk = new BerserkSpeedModifier(3, this);
        getServer().getPluginManager().registerEvents(berserk, this);
        SpeedAPI.addModifier("berserk", berserk);
        SpeedAPI.addModifier("wald", new WaldSpeedModifier(3, (float) getConfig().getDouble("walds_speed", 0.1)));
        SpeedAPI.addModifier("armor", new ArmorSpeedModifier(2, this));
        
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
    
    @Override
    public void onDisable()
    {
        home.save();
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
    
    private void loadOreStones()
    {
        oreStones = new HashMap<Material, HashMap<Material, Double>>();
        if (config.getConfigurationSection("oreStones") != null)
            for (String tool : config.getConfigurationSection("oreStones").getKeys(false))
            {
                Material mat = Material.getMaterial(tool);
                if (mat == null)
                    continue;
                
                HashMap<Material, Double> helpmap = new HashMap<Material, Double>();
                
                for (String ore : config.getConfigurationSection("oreStones." + tool).getKeys(false))
                {
                    Material mat2 = Material.getMaterial(ore);
                    if (mat2 == null)
                        continue;
                    
                    helpmap.put(mat2, config.getDouble("oreStones." + tool + "." + ore, 0D));
                }
                
                oreStones.put(mat, helpmap);
            }
    }
    
    private void loadExpSigns()
    {
        expsigns = new HashSet<Location>();
        List<String> list = config.getStringList("expsigns");
        
        for (String s : list)
            expsigns.add(parseLocation(s));
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
    
    @SuppressWarnings("deprecation")
    public void fillXPBottle(Player p, int amount)
    {
        int exp = getExp((p.getLevel() + p.getExp()));
        
        if (exp >= amount * 10 && p.getLevel() > 0)
        {
            exp -= (amount * 10);
            
            int level = (int) Math.floor(getLevel(exp));
            float progress = (float) (getLevel(exp) - Math.floor(getLevel(exp)));
            
            p.setLevel(level);
            p.setExp(progress);
            
            for (ItemStack item : p.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 1)).values())
                p.getWorld().dropItem(p.getLocation(), item);
            
            p.updateInventory();
        }
        else
            p.sendMessage("You don't have enough EXP");
    }
    
    private static int getExp(double level)
    {
        int exp = 0;
        
        double nlevel = Math.ceil(level);
        
        if (nlevel - 32 > 0)
            exp += (1 - (nlevel - level)) * (65 + (nlevel - 32) * 7);
        else if (nlevel - 16 > 0)
            exp += (1 - (nlevel - level)) * (17 + (level - 16) * 3);
        else
            exp += (1 - (nlevel - level)) * 17;
        
        level = Math.ceil(level - 1);
        
        while (level > 0)
        {
            if (level - 32 > 0)
                exp += 65 + (level - 32) * 7;
            else if (level - 16 > 0)
                exp += 17 + (level - 16) * 3;
            else
                exp += 17;
            
            level--;
        }
        
        return exp;
    }
    
    private static double getLevel(int exp)
    {
        double i = 0;
        boolean stop = true;
        
        while (stop)
        {
            i++;
            
            if (i - 32 > 0)
            {
                if (exp - (65 + (i - 32) * 7) > 0)
                    exp = (int) (exp - (65 + (i - 32) * 7));
                else
                    stop = false;
            }
            else if (i - 16 > 0)
            {
                if (exp - (17 + (i - 16) * 3) > 0)
                    exp = (int) (exp - (17 + (i - 16) * 3));
                else
                    stop = false;
            }
            else if (exp > 17)
                exp -= 17;
            else
                stop = false;
        }
        
        if (exp != 0)
            if (i - 32 > 0)
                i += exp / (65 + (i - 32) * 7);
            else if (i - 16 > 0)
                i += exp / (17 + (i - 16) * 3);
            else
                i += exp / 17D;
        
        if (exp == 0)
            i--;
        
        i--;
        return i;
    }
    
    public void reload()
    {
        reloadConfig();
        config = null;
        config = getConfig();
        loadExpSigns();
    }
}
