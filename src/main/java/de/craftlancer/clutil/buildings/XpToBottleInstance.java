package de.craftlancer.clutil.buildings;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import de.craftlancer.core.Utils;
import de.craftlancer.groups.Town;

public class XpToBottleInstance extends FeatureInstance
{
    private Block sign;
    
    public XpToBottleInstance(Town hostTown, Block sign)
    {
        super(hostTown);
        this.sign = sign;
    }
    
    public XpToBottleInstance(String key, FileConfiguration config)
    {
        super(key, config);
        sign = Utils.parseLocation(config.getString(key + ".sign")).getBlock();
    }
    
    @Override
    public FeatureType getFeatureType()
    {
        return FeatureType.XPTOBOTTLE;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.hasBlock() && e.getClickedBlock().equals(sign))
            e.getPlayer().sendMessage("Used block!");
    }
    
    @Override
    public void save(FileConfiguration config)
    {
        String key = getUniqueId().toString();
        config.set(key + ".sign", Utils.getLocationString(sign.getLocation()));
    }
    
}
