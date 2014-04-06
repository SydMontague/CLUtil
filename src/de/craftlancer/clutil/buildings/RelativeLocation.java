package de.craftlancer.clutil.buildings;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import com.sk89q.worldedit.blocks.BlockData;

public class RelativeLocation
{
    private int x;
    private int y;
    private int z;
    
    private byte southData;
    private byte westData;
    private byte northData;
    private byte eastData;
    
    public RelativeLocation(int x, int y, int z, byte southData, byte westData, byte northData, byte eastData)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.southData = southData;
        this.westData = westData;
        this.northData = northData;
        this.eastData = eastData;
    }
    
    public static RelativeLocation parseString(String s)
    {
        if (s == null)
            throw new IllegalArgumentException("This string is not allowed to be null!");
        
        String[] arr = s.split(" ");
        
        if (arr.length <= 3)
            throw new IllegalArgumentException("This string \"" + s + "\" does not fit the format. <x> <y> <z>");
        
        int x = Integer.parseInt(arr[0]);
        int y = Integer.parseInt(arr[1]);
        int z = Integer.parseInt(arr[2]);
        
        byte southData = 0;
        byte westData = 0;
        byte northData = 0;
        byte eastData = 0;
        
        if (arr.length >= 4)
            southData = Byte.parseByte(arr[3]);
        if (arr.length >= 5)
            westData = Byte.parseByte(arr[4]);
        if (arr.length >= 6)
            northData = Byte.parseByte(arr[5]);
        if (arr.length >= 7)
            eastData = Byte.parseByte(arr[6]);
        
        return new RelativeLocation(x, y, z, southData, westData, northData, eastData);
    }
    
    @Override
    public String toString()
    {
        return x + " " + y + " " + z + " " + " " + southData + " " + westData + " " + northData + " " + eastData;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public int getZ()
    {
        return z;
    }
    
    public byte getSouthData()
    {
        return southData;
    }
    
    public byte getWestData()
    {
        return westData;
    }
    
    public byte getNorthData()
    {
        return northData;
    }
    
    public byte getEastData()
    {
        return eastData;
    }
    
    public static RelativeLocation craftRelativeLocation(Object object, Material target, BlockFace facing)
    {
        if (object instanceof RelativeLocation)
            return (RelativeLocation) object;
        
        if (!(object instanceof BlockWrapper))
            throw new IllegalArgumentException("Object does not match requirements in class.");
        
        BlockWrapper block = (BlockWrapper) object;
        
        byte north;
        byte east;
        byte south;
        byte west;
        
        int initialData = block.getData();
        int typeId = block.getTypeId();
        
        switch (facing)
        {
            case NORTH:
                north = (byte) initialData;
                east = (byte) BlockData.rotate90(typeId, initialData);
                south = (byte) BlockData.rotate90(typeId, east);
                west = (byte) BlockData.rotate90(typeId, south);
                break;
            case EAST:
                east = (byte) initialData;
                south = (byte) BlockData.rotate90(typeId, initialData);
                west = (byte) BlockData.rotate90(typeId, south);
                north = (byte) BlockData.rotate90(typeId, west);
                break;
            case SOUTH:
                south = (byte) initialData;
                west = (byte) BlockData.rotate90(typeId, initialData);
                north = (byte) BlockData.rotate90(typeId, west);
                east = (byte) BlockData.rotate90(typeId, north);
                break;
            case WEST:
                west = (byte) initialData;
                north = (byte) BlockData.rotate90(typeId, initialData);
                east = (byte) BlockData.rotate90(typeId, north);
                south = (byte) BlockData.rotate90(typeId, east);
                break;
            default:
                throw new IllegalArgumentException("Facing must be either NORTH, EAST, SOUTH or WEST");
        }
        
        return new RelativeLocation(block.getX(), block.getY(), block.getZ(), south, west, north, east);
    }
}
