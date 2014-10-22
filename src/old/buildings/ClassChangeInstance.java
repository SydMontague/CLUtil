package de.craftlancer.clutil.old.buildings;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.craftlancer.core.Utils;
import de.craftlancer.groups.Town;

public class ClassChangeInstance extends FeatureInstance
{
    private Block waldl;
    private Block krieger;
    private Block schurke;
    
    public ClassChangeInstance(Town town, Block waldl, Block krieger, Block schurke)
    {
        super(town);
        this.waldl = waldl;
        this.krieger = krieger;
        this.schurke = schurke;
    }
    
    public ClassChangeInstance(String key, FileConfiguration config)
    {
        super(key, config);
        waldl = Utils.parseLocation(config.getString(key + ".waldl")).getBlock();
        krieger = Utils.parseLocation(config.getString(key + ".krieger")).getBlock();
        schurke = Utils.parseLocation(config.getString(key + ".schurke")).getBlock();
    }
    
    @Override
    public FeatureType getFeatureType()
    {
        return FeatureType.CLASSCHANGE;
    }
    
    @Override
    public void save(FileConfiguration config)
    {
        String key = getUniqueId().toString();
        config.set(key + ".waldl", Utils.getLocationString(waldl.getLocation()));
        config.set(key + ".krieger", Utils.getLocationString(krieger.getLocation()));
        config.set(key + ".schurke", Utils.getLocationString(schurke.getLocation()));
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        if (e.getClickedBlock().equals(waldl))
            handleWaldlaeuferInteract(e.getPlayer());
        
        else if (e.getClickedBlock().equals(krieger))
            handleKriegerInteract(e.getPlayer());
        
        else if (e.getClickedBlock().equals(schurke))
            handleSchurkeInteract(e.getPlayer());
    }
    
    private void handleSchurkeInteract(Player player)
    {
        // TODO Auto-generated method stub
        
    }
    
    private void handleKriegerInteract(Player player)
    {
        // TODO Auto-generated method stub
        
    }
    
    private void handleWaldlaeuferInteract(Player player)
    {
        // TODO Auto-generated method stub
        
    }
}
