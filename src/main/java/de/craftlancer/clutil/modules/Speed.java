package de.craftlancer.clutil.modules;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.clutil.speed.ArmorSpeedModifier;
import de.craftlancer.clutil.speed.BerserkSpeedModifier;
import de.craftlancer.clutil.speed.SneakSpeedModifier;
import de.craftlancer.clutil.speed.WaldSpeedModifier;
import de.craftlancer.clutil.speed.WeightSpeedModifier;
import de.craftlancer.speedapi.SpeedAPI;

public class Speed extends Module
{
    private ArmorSpeedModifier armorMod;
    private BerserkSpeedModifier berserkMod;
    private SneakSpeedModifier sneakMod;
    private WaldSpeedModifier waldMod;
    private WeightSpeedModifier weightMod;
    
    public Speed(CLUtil plugin)
    {
        super(plugin);
        if (getConfig().getBoolean("armorMod.enabled", false))
        {
            double ironSpeed = getConfig().getDouble("armorMod.ironSpeed", 0.18D);
            double diaSpeed = getConfig().getDouble("armorMod.diaSpeed", 0.16D);
            
            armorMod = new ArmorSpeedModifier(getConfig().getInt("armorMod.priority", 5), ironSpeed, diaSpeed);
            SpeedAPI.addModifier("armor", armorMod);
        }
        if (getConfig().getBoolean("berserker.enabled", false))
        {
            double maxDmg = getConfig().getDouble("berserker.maxDmg", 2);
            float maxSpeed = (float) getConfig().getDouble("berserker.maxSpeed", 0.5);
            int maxCombo = getConfig().getInt("berserker.maxCombo", 7);
            long duration = getConfig().getLong("berserker.duration", 10000L);
            int priority = getConfig().getInt("berserker.priority", 4);
            
            berserkMod = new BerserkSpeedModifier(priority, maxSpeed, maxDmg, maxCombo, duration);
            SpeedAPI.addModifier("berserker", berserkMod);
            getPlugin().getServer().getPluginManager().registerEvents(berserkMod, getPlugin());
        }
        if (getConfig().getBoolean("sneakMod.enabled", false))
        {
            sneakMod = new SneakSpeedModifier(getConfig().getInt("sneakMod.priority", 5), (float) getConfig().getDouble("sneakMod.amount", 0.1D));
            SpeedAPI.addModifier("sneak", sneakMod);
        }
        if (getConfig().getBoolean("waldMod.enabled", false))
        {
            waldMod = new WaldSpeedModifier(getConfig().getInt("waldMod.priority", 5), (float) getConfig().getDouble("waldMod.speed", 0.1));
            SpeedAPI.addModifier("wald", waldMod);
        }
        if (getConfig().getBoolean("weightMod.enabled", false))
        {
            weightMod = new WeightSpeedModifier(getConfig().getInt("weightMod.priority", 5));
            SpeedAPI.addModifier("weightMod", weightMod);
        }
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.SPEED;
    }
    
}
