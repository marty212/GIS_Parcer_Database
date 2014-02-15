import java.util.Vector;

/**
 * An entry to my hashtable.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class HashEntry {
	private String         featureName; //Obvious
	private String         stateAbbr;   //Same
	private Vector<Long>   offset;      //The current file offset

	/**
	 * Makes a hashentry.
	 * @param feature the feature name
	 * @param state   the state abbreviation
	 * @param off     the file offset
	 */
	public HashEntry(String feature, String state, long off)
	{
		featureName = feature;
		stateAbbr   = state;
		offset      = new Vector<Long>();
		offset.add(off);
	}

	/**
	 * Gets the offset.
	 * @return the offset
	 */
	public Vector<Long> getOffset()
	{
		return offset;
	}

	/**
	 * Gets feature name.
	 * @return the feature
	 */
	public String getFeature()
	{
		return featureName;
	}

	/**
	 * Gets state abbreviation.
	 * @return the state abbreviation
	 */
	public String getState()
	{
		return stateAbbr;
	}

	/**
	 * Prepares the value to hash.
	 * @return returns that value
	 */
	public String toHash()
	{
		return featureName + ":" + stateAbbr;
	}

	/**
	 * Contains method.
	 * @param off what we want
	 * @return if we found it
	 */
	public boolean contains(long off)
	{
		return offset.contains(off);
	}
	
	/**
	 * Removes an element of offset.
	 * @param off the element being removed.
	 */
	public void remove(long off)
	{
		offset.remove(off);
	}
	
	/**
	 * Adds offset.
	 * @param off the element being added
	 */
	public void add(long off)
	{
		offset.add(off);
	}
	/**
	 * A toString method.
	 * @return the string representation of the object
	 */
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder(); //For return
		s.append("[");
		s.append(featureName);
		s.append(":");
		s.append(stateAbbr);
		s.append(",");
		for(Long i : offset)
		{
			s.append(" [" + i + "]");
		}
		s.append("]\n");
		return s.toString();
	}

	/**
	 * Sees if the entries are equal (not using offset!).
	 * @return whether they are equal
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof HashEntry)
		{
			HashEntry h = (HashEntry) o; //Casting
			return h.featureName.equals(featureName)
					&& h.stateAbbr.equals(stateAbbr);
		}
		else if (o instanceof Controller.Buffer)
		{
			Controller.Buffer b = (Controller.Buffer) o; //Casting
			return equals(new HashEntry(b.record.report("'fN'"),
					b.record.report("'sAC'"), 0));
		}
		return false;
	}
	
	/**
	 * Makes an elfHash of this object.
	 * @return the hash value.
	 */
	@Override
	public int hashCode()
	{
		return elfHash(toHash());
	}
	/**
	 * This is the hashing method from course notes.
	 * @param toHash The String I am hashing
	 * @return the hash value as an int
	 */
	public static int elfHash(String toHash) {
		int	hashValue = 0;
		for( int Pos = 0; Pos < toHash.length(); Pos++) { // use all elements
			hashValue = (hashValue << 4) + toHash.charAt(Pos);	// shift/mix
			int	hiBits = hashValue & 0xF0000000; //get high nybble
			if(hiBits != 0)
				hashValue ^= hiBits >> 24; // xor high nybble with second nybble
			hashValue &= ~hiBits; // clear high nybble
		}
		return hashValue;
	}
}
