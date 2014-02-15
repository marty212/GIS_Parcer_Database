

// -------------------------------------------------------------------------
/**
 *  Holds the longitude and latitude records.
 *
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class Coordinates
{
    // -------------------------------------------------------------------------
    /**
     *  Enum which allows direction to be typesafe.
     *
     *  @author borstad
     *  @version 01.29.2013
     */
    public enum Direction
    {
        /**
         * North
         */
        N ("N"),
        /**
         * South
         */
        S ("S"),
        /**
         * West
         */
        W ("W"),
        /**
         * East
         */
        E ("E"),
        /**
         * Unknown
         */
        U ("UNKNOWN");
        private String letter;  //Holds the letter
        /**
         * Constructs the enum for direction.
         * @param s the String provided by enum
         */
        Direction(final String s)
        {
            letter = s;
        }
        // ----------------------------------------------------------
        /**
         * Returns the letter of the direction.
         * @return a string letter
         */
        public String getDir()
        {
            return letter;
        }
        /**
         * Returns the long direction name.
         * @return the direction name
         */
        public String getDirection()
        {
        	if (letter == "N") {
        		return "North";
        	}
        	else if (letter == "S") {
        		return "South";
        	}
        	else if (letter == "E") {
        		return "East";
        	}
        	else if (letter == "W") {
        		return "West";
        	}
        	return "Unknown";
        }
    }
    private final String    d;   //The degrees
    private final String    m;   //The minutes
    private final String    s;   //The seconds
    private final Direction dir; //The direction
    // ----------------------------------------------------------
    /**
     * Create a new Coordinates object that specific fields can be taken from.
     * @param coor       the coordinate being parsed
     * @param isLong     if it is a longitude coordinate or not
     * @throws Exception if there is something wrong with inputs
     */
    public Coordinates(final String coor, final boolean isLong)
    {
        if (coor.equals("UNKNOWN") || coor.equals(""))
        {
            d   = "";
            m   = "";
            s   = "";
            dir = Direction.U;
            return;
        }
        d = coor.substring(0, isLong ? 3 : 2);
        m = coor.substring(isLong ? 3 : 2, isLong ? 5 : 4);
        s = coor.substring(isLong ? 5 : 4, isLong ? 7 : 6);
        if (isLong)
        {
            dir = coor.contains("E") ? Direction.E : Direction.W;
        }
        else
        {
            dir = coor.contains("N") ? Direction.N : Direction.S;
        }
    }
    // ----------------------------------------------------------
    /**
     * Gets the degrees.
     * @return returns degree field
     */
    public String getD()
    {
        return Integer.toString(Integer.parseInt(d)); //Removes leading 0 if there is one.
    }
    // ----------------------------------------------------------
    /**
     * Gets the minutes.
     * @return the minutes
     */
    public String getM()
    {
        return Integer.toString(Integer.parseInt(m)); //Removes leading 0 if there is one.
    }
    /**
     * Gets the seconds.
     * @return the seconds.
     */
    public String getS()
    {
        return Integer.toString(Integer.parseInt(s)); //Removes leading 0 if there is one.
    }
    /**
     * Gets the direction.
     * @return the direction
     */
    public Direction getDir()
    {
        return dir;
    }
    /**
     * Returns a toString of this object.
     * @return a string of the coordinates
     */
    public String toString()
    {
        return d + m + s + dir.getDir();
    }
    
    /**
     * Converts a coordinate to seconds
     * @param coor the coordinate
     * @param isLong is it longitude
     * @return the seconds representation of the coordinate
     */
    public static int convertToSec(String coor, Boolean isLong)
    {
    	int d = Integer.parseInt(coor.substring(0, coor.length() - 5));
        int m = Integer.parseInt(coor.substring(coor.length() - 5, coor.length() - 3));
        int s = Integer.parseInt(coor.substring(coor.length() - 3, coor.length() - 1));
        s += m * 60 + d * 3600;
        if (isLong)
        {
        	s *= coor.contains("E") ? 1 : -1;
        }
        else
        {
            s *= coor.contains("N") ? 1 : -1;
        }
        return s;
    }
}