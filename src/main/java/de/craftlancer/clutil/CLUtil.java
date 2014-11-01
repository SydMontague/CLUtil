package de.craftlancer.clutil;

import java.io.File;
import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;

import de.craftlancer.clutil.modules.AdvancedEnchantments;
import de.craftlancer.clutil.modules.CaptureTheToken;
import de.craftlancer.clutil.modules.ClassChanger;
import de.craftlancer.clutil.modules.ColoredNametag;
import de.craftlancer.clutil.modules.CustomEnchantments;
import de.craftlancer.clutil.modules.DebugMode;
import de.craftlancer.clutil.modules.DistanceShot;
import de.craftlancer.clutil.modules.EffectWeapons;
import de.craftlancer.clutil.modules.GriefBlock;
import de.craftlancer.clutil.modules.HeadHunter;
import de.craftlancer.clutil.modules.Home;
import de.craftlancer.clutil.modules.OreStones;
import de.craftlancer.clutil.modules.OwnHealth;
import de.craftlancer.clutil.modules.PotionRebalance;
import de.craftlancer.clutil.modules.PumpkinBandit;
import de.craftlancer.clutil.modules.PvPRebalance;
import de.craftlancer.clutil.modules.RandomChests;
import de.craftlancer.clutil.modules.Speed;
import de.craftlancer.clutil.modules.TokenModule;
import de.craftlancer.clutil.modules.Tracking;
import de.craftlancer.clutil.modules.UtilModule;
import de.craftlancer.clutil.modules.WorkingSkills;

public class CLUtil extends JavaPlugin
{
    private HashMap<ModuleType, Module> modules = new HashMap<>();
    
    @Override
    public void onEnable()
    {
        if (!new File(this.getDataFolder(), "config.yml").exists())
            saveDefaultConfig();
        
        loadModules();
    }
    
    public DebugMode getDebugModule()
    {
        if (modules.containsKey(ModuleType.DEBUG))
            return (DebugMode) modules.get(ModuleType.DEBUG);
        
        return null;
    }
    
    private void loadModules()
    {
        for (ModuleType type : ModuleType.values())
        {
            if (isDeactivated(type))
                continue;
            
            modules.put(type, craftModule(type));
            getLogger().info("Module " + type.name() + " loaded!");
        }
    }
    
    private boolean isDeactivated(ModuleType type)
    {
        return !getConfig().getBoolean("modules." + type.name(), true);
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
            case DISTANCESHOT:
                return new DistanceShot(this);
            case RANDOMCHESTS:
                return new RandomChests(this);
            case CUSTOMENCHANTS:
                return new CustomEnchantments(this);
            case TOKEN:
                return new TokenModule(this);
            case TRACKING:
                return new Tracking(this);
            case DEBUG:
                return new DebugMode(this);
            case COLOREDNAMETAGS:
                return new ColoredNametag(this);
            case POTIONREBALANCE:
                return new PotionRebalance(this);
            case PVPREBALANCE:
                return new PvPRebalance(this);
            case WORKINGSKILLS:
                return new WorkingSkills(this);
            case CAPTURETHETOKEN:
                return new CaptureTheToken(this);
            case CLASSCHANGE:
                return new ClassChanger(this);
            case GRIEFBLOCK:
                return new GriefBlock(this);
            case UTILITY:
                return new UtilModule(this);
            default:
                break;
        }
        
        throw new IllegalArgumentException("Illegal ModuleType detected!");
    }
    
    @Override
    public void onDisable()
    {
        for (Module mod : modules.values())
            mod.onDisable();
        
        getServer().getScheduler().cancelTasks(this);
    }
}
