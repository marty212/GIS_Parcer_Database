

/**
 * Class used to represent date.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class CustomDate
{
	private final int TWO = 2; //Represents 2
	private static String FORMAT = "mm/dd/yyyy"; //So I don't need to type this in a bunch of times.
	
	private final int month; //The month
	private final int day;   //The day
	private final int year;  //The year
	
	/**
	 * Private constructor.
	 * @param nMonth the new month
	 * @param nDay   the new day
	 * @param nYear  the new year
	 */
	private CustomDate(int nMonth, int nDay, int nYear)
	{
		month = nMonth;
		day   = nDay;
		year  = nYear;
	}
	/**
	 * Returns month.
	 * @return the month
	 */
	public int getMonth()
	{
		return month;
	}
	/**
	 * Returns day.
	 * @return the day
	 */
	public int getDay()
	{
		return day;
	}
	/**
	 * Returns year.
	 * @return the year
	 */
	public int getYear()
	{
		return year;
	}
	
	/**
	 * Sees if this is equal to another date.
	 * @param o the object being compared
	 * @return whether it is equal
	 */
	public boolean equals(Object o)
	{
		if (o instanceof CustomDate)
		{
			CustomDate d = (CustomDate)o; //Quick cast
			return d.day == day && d.month == month && d.year == year;
		}
		return false;
	}
	
	/**
	 * Calls getFormattedString with a predone format.
	 * @return the predone format string
	 */
	public String getStdFormat()
	{
		return getFormattedString(FORMAT);
	}
	/**
	 * Gets a fancy string.
	 * @param format needs mm, dd, yy or yyyy or an exception will be called!
	 * @return the string that fits the format
	 */
	public String getFormattedString(final String format)
	{
		String workingRet = format; //Copy of the input
		if (workingRet.contains("mm"))
		{
			workingRet = workingRet.replace("mm", month > 9 ? Integer.toString(month) : "0" + Integer.toString(month));
		}
		else
		{
			throw new IllegalArgumentException("mm was not found in string!");
		}
		if (workingRet.contains("yyyy"))
		{
			workingRet = workingRet.replace("yyyy", Integer.toString(year));
		}
		else if  (workingRet.contains("yy"))
		{
			workingRet = workingRet.replace("yy", Integer.toString(year).substring(TWO));
		}
		else
		{
			throw new IllegalArgumentException("Invalid year format!");
		}
		if (workingRet.contains("dd"))
		{
			workingRet = workingRet.replace("dd", day > 9 ? Integer.toString(day) : "0" + Integer.toString(month));
		}
		else
		{
			throw new IllegalArgumentException("dd was not found in string!");
		}
		return workingRet;
	}
	/**
	 * Changes the standard format.
	 * @param format must meet the same specifications as getFormattedString
	 */
	public static void setFormat(final String format)
	{
		FORMAT = format;
	}
	/**
	 * Builds the class.
	 * @author Borstad
	 * @version 01.31.2013
	 * @project File Navigation
	 */
	public static class Builder
	{
		private int month = 0; //Month
		private int day   = 0; //Day
		private int year  = 0; //Year
		/**
		 * Builds the class.
		 * @return the new date
		 */
		public CustomDate build()
		{
			return new CustomDate(month, day, year);
		}
		/**
		 * Sets the month for new date.
		 * @param newMonth the new month
		 * @return this object
		 */
		public Builder setMonth(final int newMonth)
		{
			month = newMonth;
			return this;
		}
		/**
		 * Sets day for new date.
		 * @param newDay the new day
		 * @return this object
		 */
		public Builder setDay(final int newDay)
		{
			day = newDay;
			return this;
		}
		/**
		 * Sets year for new date.
		 * @param newYear the year
		 * @return this object
		 */
		public Builder setYear(final int newYear)
		{
			year = newYear;
			return this;
		}
	}
}
