package de.craftlancer.clutil.buildings;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import de.craftlancer.groups.Town;

public class XpToBottleFeature implements FeatureBuilding
{
    private RelativeLocation signLocation;
    
    public XpToBottleFeature(Map<String, RelativeLocation> blockLoc)
    {
        if (!blockLoc.containsKey("sign"))
            throw new IllegalArgumentException("The key 'sign' is not defined, but mandatory for this feature!");
        else
            signLocation = blockLoc.get("sign");
    }
    
    @Override
    public void place(Block block, Town town, int facing)
    {
        
        placeSign(block.getRelative(signLocation.getX(), signLocation.getY(), signLocation.getZ()), town, facing);
    }
    
    @SuppressWarnings("deprecation")
    private void placeSign(Block relative, Town town, int facing)
    {
        byte data;
        switch (facing % 4)
        {
            case 0:
                data = signLocation.getSouthData();
                break;
            case 1:
                data = signLocation.getWestData();
                break;
            case 2:
                data = signLocation.getNorthData();
                break;
            case 3:
                data = signLocation.getEastData();
                break;
            default:
                data = 0;
        }
        
        relative.setType(Material.SIGN_POST);
        relative.setData(data, false);
        
        Sign sign = (Sign) relative.getState();
        sign.setLine(1, "Will it blend?");
        sign.setLine(2, "Thats the question!");
        sign.update();
        
        FeatureManager.getInstance().addFeature(new XpToBottleInstance(town, relative));
    }
}
