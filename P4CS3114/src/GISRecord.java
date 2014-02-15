/**
 * This class parses the GIS record that is properly formatted from the project specifications.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class GISRecord
{
	/**
	 * Variables are explained in setup.
	 */
    private int         fID, sNC, cNC, fEM, fEF;
    private String      fN, fC, sAC, cN, mN;
    private CustomDate  dC, dE;
    private Coordinates c1, c2, c3, c4;
    private double      c1s, c2s, c3s, c4s;

    /**
     * Empty constructor.  Must use setup once before using report.
     */
    public GISRecord()
    {
    	//This is empty on purpose.
    }
    /**
     * This constructs the record so that it can be used immediately.
     * @param record The record being parsed.
     * @throws Exception  Whatever happens in setup.
     */
    public GISRecord(String record) throws Exception
    {
       setUp(record);
    }
    /**
     * Parses the record into usable fields.
     * Each variable is explained here.
     * @param record The record being parsed.
     * @throws Exception  If I can't parse the record.
     */
    public void setUp(String record) throws Exception
    {
        String rec[] = record.split("\\|");
        if (rec.length < 19)
        {
            System.err.println("Error in construction of GISRecord");
            throw new Exception();
        }
        fID = Integer.parseInt(rec[0]);                                //Feature ID
        fN  = rec[1];                                                  //Feature Name
        fC  = rec[2];                                                  //Feature Class
        sAC = rec[3];                                                  //State alphabetic code
        sNC = Integer.parseInt(rec[4]);                                //State numeric code
        cN  = rec[5];                                                  //County name
        cNC = Integer.parseInt(rec[6]);                                //Count Numeric Code
        c1  = new Coordinates(rec[7], false);                          //Primary Latitude (DMS)
        c2  = new Coordinates(rec[8], true);                           //Primary Longitude (DMS)
        c1s = rec[9].isEmpty() || rec[9].equals("UNKNOWN") ? 0 : Double.parseDouble(rec[9]);//Primary Latitude (dec deg)
        c2s = rec[10].isEmpty() || rec[10].equals("UNKNOWN") ? 0 : Double.parseDouble(rec[10]);//Primary Longitude (dec deg)
        c3  = new Coordinates(rec[11], false);                         //Source Latitude (DMS)
        c4  = new Coordinates(rec[12], true);                          //Source Longitude (DMS)
        c3s = !rec[13].isEmpty() ? Double.parseDouble(rec[13]) : 0;    //Source Latitude (dec deg)
        c4s = !rec[14].isEmpty() ? Double.parseDouble(rec[14]) : 0;    //Source Longitude (dec deg)
        fEM = rec[15].isEmpty() ? -10000000 : Integer.parseInt(rec[15]); //Feature elevation in meters
        fEF = rec[16].isEmpty() ? -10000000 : Integer.parseInt(rec[16]); //Feature elevation in feet
        mN  = rec[17];                                                 //Map name
        String[] tempStore = rec[18].split("/"); //Used to parse date.
        if (!rec[18].isEmpty())
        	dC = new CustomDate.Builder().setMonth(Integer.parseInt(tempStore[0]))/** Date created */
        	.setDay(Integer.parseInt(tempStore[1]))
        	.setYear(Integer.parseInt(tempStore[2]))
        	.build();
        else
        	dC = null;
        /**
         * Date edited is not an optional field and should be there, but just in case.
         */
        if (rec.length == 20)
        {
        	tempStore = rec[19].split("/");
        }
        dE = new CustomDate.Builder().setMonth(Integer.parseInt(tempStore[0]))
        		/** Date edited. */
        		.setDay(Integer.parseInt(tempStore[1]))
        		.setYear(Integer.parseInt(tempStore[2]))
        		.build();
    }
    /**
     * Reports back the given string.
     * @precondition setup ran once
     * @return the given string that was parsed
     */
    public String report()
    {
    	return fID + "|" + fN + "|" + fC + "|" + sAC + "|" + sNC + "|" + cN
    			+ "|" + cNC + "|" + c1.toString() + "|" + c2.toString() + "|"
    				+ c1s + "|" + c2s + "|" + c3.toString() + "|" + c4.toString()
    				+ "|" + c3s + "|" + c4s + "|" + fEM
    				+ "|" + fEF + "|" + mN + "|" + dC.getStdFormat() + "|" + dE.getStdFormat();
    }
    
    /**
     * Reports fields that are not empty.
     * @return the non-empty fields
     */
    public String reportRel()
    {
    	StringBuilder s = new StringBuilder(); //For return
		s.append(sizeS("Feature ID") + ": " + Integer.toString(fID) + "\n");
		if(!fN.isEmpty())
			s.append(sizeS("Feature Name") + ": " + fN + "\n");
		if (!fC.isEmpty())
			s.append(sizeS("Feature Cat") + ": " + fC + "\n");
		if(!sAC.isEmpty())
			s.append(sizeS("State") + ": " + sAC + "\n");
		if(!cN.isEmpty())
			s.append(sizeS("County Name") + ": " + cN + "\n");
		if(!c1.toString().isEmpty())
			s.append(sizeS("Latitude") + ": " + c1.toString() + "\n");
		if(!c2.toString().isEmpty())
			s.append(sizeS("Longitude") + ": " + c2.toString() + "\n");
		if(!c3.toString().equals("UNKNOWN"))
			s.append(sizeS("Src Lat") + ": " + c3.toString() + "\n");
		if(!c4.toString().equals("UNKNOWN"))
			s.append(sizeS("Src Long") + ": " + c4.toString() + "\n");
		if(fEF > -10000000)
			s.append(sizeS("Elev in ft") + ": " + fEF + "\n");
		if(!mN.isEmpty())
			s.append(sizeS("USGS Quad") + ": " + mN + "\n");
		s.append(sizeS("Date created") + ": " + dC.getStdFormat() + "\n");
		if(!dC.equals(dE))
			s.append(sizeS("Date mod") + ": " + dE.getStdFormat() + "\n");
    	return s.toString();
    }
    
    /**
     * 
     */
    private String sizeS(String s)
    {
    	String ret = s;
    	while(ret.length() < 14)
    	{
    		ret+= " ";
    	}
    	return ret;
    }
    /**
     * Returns a custom report.  Surround variable names with a '
     * Uses a case statement to process each variable.
     * @precondition the pattern can not have any variable names by
     *               themselves if not part of pattern.
     * @precondition setup ran once
     * @param pattern the pattern being used to generate a report
     * @return the report requested
     */
    public String report(final String pattern)
    {
		String parse[] = pattern.split("'"); //Holds the pattern
		StringBuilder build = new StringBuilder();//A mutable String
		for (String s : parse)//Runs through pattern
		{
			String append = ""; //What I will append each time.
			if (s.equals("fID"))
			{
				append = Integer.toString(fID);
			}
			else if (s.equals("fN")) {
				append = fN;
			}
			else if (s.equals("fC")) {
				append = fC;
			}
			else if (s.equals("sAC")) {
				append = sAC;
			}
			else if (s.equals("sNC")) {
				append = Integer.toString(sNC);
			}
			else if (s.equals("c1")) {
				//append = c1.getD() + "d " + c1.getM() + "m " + c1.getS() + "s " + c1.getDir().getDirection();
				append = c1.toString();
			}
			else if (s.equals("c2")) {
				//append = c2.getD() + "d " + c2.getM() + "m " + c2.getS() + "s " + c2.getDir().getDirection();
				append = c2.toString();
			}
			else if (s.equals("c1s")) {
				append = Double.toString(c1s);
			}
			else if (s.equals("c2s")) {
				append = Double.toString(c2s);
			}
			else if (s.equals("c3")) {
				append = c3.getD() + "d " + c1.getM() + "m "
						+ c3.getS() + "s " + c3.getDir().getDirection();
			}
			else if (s.equals("c4")) {
				append = c4.getD() + "d " + c4.getM() + "m "
						+ c4.getS() + "s " + c4.getDir().getDirection();
			}
			else if (s.equals("c3s")) {
				append = Double.toString(c3s);
			}
			else if (s.equals("c4s")) {
				append = Double.toString(c4s);
			}
			else if (s.equals("fEM")) {
				append = Integer.toString(fEM);
			}
			else if (s.equals("fEF")) {
				append = Integer.toString(fEF);
			}
			else if (s.equals("mN")) {
				append = mN;
			}
			else if (s.equals("dC")) {
				append = dC.getStdFormat();
			}
			else if (s.equals("dE")) {
				append = dE.getStdFormat();
			}
			else if (s.equals("cN"))
			{
				append = cN;
			}
			else {
				append = s;
			}
			build.append(append);
		}
		return build.toString();
    }
    
    /**
     * Get latitude in seconds.
     * @return the latitude in seconds
     */
    public int getLatS()
    {
    	return (c1.getDir() == Coordinates.Direction.N ? 1 : -1) 
    			* (Integer.parseInt(c1.getD()) * 3600 +
    					Integer.parseInt(c1.getM()) * 60
    					+ Integer.parseInt(c1.getS()));
    }
    
    /**
     * Get longitude in seconds.
     * @return the longitude in seconds
     */
    public int getLongS()
    {
    	return (c2.getDir() == Coordinates.Direction.E ? 1 : -1) 
    			* (Integer.parseInt(c2.getD()) * 3600
    					+ Integer.parseInt(c2.getM()) * 60
    					+ Integer.parseInt(c2.getS()));
    }
}