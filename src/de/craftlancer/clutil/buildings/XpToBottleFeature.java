package de.craftlancer.clutil.buildings;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class XpToBottleFeature implements FeatureBuilding
{
    private RelativeLocation sign;
    
    public XpToBottleFeature(Map<String, RelativeLocation> blockLoc)
    {
        if (!blockLoc.containsKey("sign"))
            throw new IllegalArgumentException("The key 'sign' is not defined, but mandatory for this feature!");
        else
            sign = blockLoc.get("sign");
    }
    
    @Override
    public void place(Block block)
    {
        placeSign(block.getRelative(sign.getX(), sign.getY(), sign.getZ()));
    }
    
    private void placeSign(Block relative)
    {
        relative.setType(Material.SIGN_POST);
        Sign sign = (Sign) relative.getState();
        sign.setLine(1, "Will it blend?");
        sign.setLine(2, "Thats the question!");
        sign.update();
        // TODO Feature Backend
        
    }
    
}
