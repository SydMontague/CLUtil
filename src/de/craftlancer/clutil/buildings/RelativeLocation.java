package de.craftlancer.clutil.buildings;

public class RelativeLocation
{
    private int x;
    private int y;
    private int z;
    
    public RelativeLocation(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static RelativeLocation parseString(String s)
    {
        if (s == null)
            throw new IllegalArgumentException("This string is not allowed to be null!");
        
        String[] arr = s.split(" ");
        
        if (arr.length != 3)
            throw new IllegalArgumentException("This string \"" + s + "\" does not fit the format. <x> <y> <z>");
        
        int x = Integer.parseInt(arr[0]);
        int y = Integer.parseInt(arr[1]);
        int z = Integer.parseInt(arr[2]);
        
        return new RelativeLocation(x, y, z);
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
}
