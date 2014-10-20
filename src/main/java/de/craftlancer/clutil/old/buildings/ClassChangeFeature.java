package de.craftlancer.clutil.old.buildings;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import de.craftlancer.groups.Town;

public class ClassChangeFeature implements FeatureBuilding
{
    private static final String NAME = "ClassChange";
    
    private RelativeLocation waldlSign;
    private RelativeLocation kriegerSign;
    private RelativeLocation schurkeSign;
    
    public ClassChangeFeature(Map<String, ?> blockLoc, BlockFace facing)
    {
        if (!blockLoc.containsKey("waldlSign") || !blockLoc.containsKey("kriegerSign") || !blockLoc.containsKey("schurkeSign"))
            throw new IllegalArgumentException("The key 'sign' is not defined, but mandatory for this feature!");
        
        waldlSign = RelativeLocation.craftRelativeLocation(blockLoc.get("waldlSign"), Material.WALL_SIGN, facing);
        kriegerSign = RelativeLocation.craftRelativeLocation(blockLoc.get("kriegerSign"), Material.WALL_SIGN, facing);
        schurkeSign = RelativeLocation.craftRelativeLocation(blockLoc.get("schurkeSign"), Material.WALL_SIGN, facing);
    }
    
    @Override
    public void place(Block block, Town town, int playerFacing)
    {
        Block waldl = block.getRelative(waldlSign.getX(), waldlSign.getY(), waldlSign.getZ());
        Block krieger = block.getRelative(kriegerSign.getX(), kriegerSign.getY(), kriegerSign.getZ());
        Block schurke = block.getRelative(schurkeSign.getX(), schurkeSign.getY(), schurkeSign.getZ());
        
        placeSign(waldlSign, waldl, town, playerFacing, "", "[Rollenwechsel]", "Waldläufer", "");
        placeSign(kriegerSign, krieger, town, playerFacing, "", "[Rollenwechsel]", "Krieger", "");
        placeSign(schurkeSign, schurke, town, playerFacing, "", "[Rollenwechsel]", "Schurke", "");
        
        FeatureManager.getInstance().addFeature(new ClassChangeInstance(town, waldl, krieger, schurke));
    }
    
    @SuppressWarnings("deprecation")
    private static void placeSign(RelativeLocation signLoc, Block relative, Town town, int facing, String line1, String line2, String line3, String line4)
    {
        byte data;
        switch (facing % 4)
        {
            case 0:
                data = signLoc.getSouthData();
                break;
            case 1:
                data = signLoc.getWestData();
                break;
            case 2:
                data = signLoc.getNorthData();
                break;
            case 3:
                data = signLoc.getEastData();
                break;
            default:
                data = 0;
        }
        
        relative.setType(Material.SIGN_POST);
        relative.setData(data, false);
        
        Sign sign = (Sign) relative.getState();
        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);
        sign.update();
    }
    
    @Override
    public String getName()
    {
        return NAME;
    }
    
    @Override
    public void save(String name, FileConfiguration config)
    {
        config.set(name + ".feature.type", getType().name());
        
        config.set(name + ".feature.sign", waldlSign.toString());
        config.set(name + ".feature.sign", kriegerSign.toString());
        config.set(name + ".feature.sign", schurkeSign.toString());
    }
    
    @Override
    public FeatureType getType()
    {
        return FeatureType.CLASSCHANGE;
    }
    
}
