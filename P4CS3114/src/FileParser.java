import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Parses a file to the database and provides access to database.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class FileParser {
	private HashTable<HashEntry>    h;          //The hashtable.
	private RandomAccessFile        data;       //The database.
	private prQuadtree<Point<Long>> tree;       //The tree.
	private int                     lastImport; //Last import info.
	private int                     lastImpLoc; //Same as above.
	
	/**
	 * Sets up the file parser.
	 * Also logs the giant header for the database file.
	 * @param setH     sets the hashtable
	 * @param database the file the database is going
	 * @param nTree    sets the tree
	 */
	public FileParser(HashTable<HashEntry> setH,
			File database, prQuadtree<Point<Long>> nTree)
	{
		lastImport = 0;
		lastImpLoc = 0;
		h = setH;
		tree = nTree;
		database.delete();
		try {
			data = new RandomAccessFile(database, "rw");
			data.writeBytes("FEATURE_ID|FEATURE_NAME|" +
					"FEATURE_CLASS|STATE_ALPHA|STATE_NUMERIC|" +
					"COUNTY_NAME|COUNTY_NUMERIC|PRIMARY_LAT_DMS" +
					"|PRIM_LONG_DMS|PRIM_LAT_DEC|" +
					"PRIM_LONG_DEC|SOURCE_LAT_DMS|SOURCE_LONG_DMS|" +
					"SOURCE_LAT_DEC|SOURCE_LONG_DEC|" +
					"ELEV_IN_M|" +
					"ELEV_IN_FT|MAP_NAME|DATE_CREATED|DATE_EDITED\n");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Imports a database file.
	 * @param database the database we are adding to ours
	 * @return the longest probe sequence
	 */
	public int imp(File database)
	{
		try {
			RandomAccessFile file = new RandomAccessFile(database, "r");
			//Lets open up that file.
			String rec = file.readLine(); //Getting rid of first line.
			rec = file.readLine();
			int ret = 0;
			lastImport = 0;
			lastImpLoc = 0;
			/**
			 * Reads through the GIS file and prints it to the results file.
			 */
			while(rec != null)
			{
				GISRecord record = null;
				try {
					record = new GISRecord(rec);//Parses the record
				}catch(Exception e){
					System.err.println("Error in parsing record," +
							"skipping record!");
					rec = file.readLine();
					continue;
				}
				long currentOffset = data.getFilePointer();//The offset
				                                           //for this file.
				data.writeBytes(rec + "\n");
				lastImport++;
				if (!record.report("'c1'").equals(""))
				{
					Point<Long> p = new Point<Long>(record.getLongS(),
							record.getLatS());
					//Making point to try and insert.
					if (tree.insert(p))
					{
						p.add(currentOffset);
					}
					else
					{
						p = tree.find(p);
						if (p != null)
						{
							p.add(currentOffset);
						}
					}
					if (p != null)
					{
						HashEntry elem = new HashEntry(
								record.report("'fN'"),
								record.report("'sAC'"),
								currentOffset);
						//Make element to store in hashtable
						HashEntry r = h.retrive(elem);
						//Retrieve the record in has table if
						//possible and add offset to that element
						//Else add it to the hashtable
						int i = 0;
						if(r == null)
						{
							i = h.add(elem);
						}
						else
						{
							r.add(currentOffset);
						}
						//i is the probe sequence
						if (i > ret)
						{
							ret = i;
						}
					}
					lastImpLoc++;
				}
				rec = file.readLine();
			}
			file.close();
			return ret;
		} catch (FileNotFoundException e) {
			System.err.println("Error in accessing file in import.  " +
					"Results may be wrong after this!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error in accessing file in import.  " +
					"Results may be wrong after this!");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Something went wrong!");
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Get a GISRecord at an offset.
	 * @precondition the GISRecord exists there
	 * @param offset the offset
	 * @return the GISRecord that was there
	 */
	public GISRecord get(long offset)
	{
		try {
			long beforeOff = data.getFilePointer();
			data.seek(offset);
			GISRecord ret = new GISRecord(data.readLine());
			data.seek(beforeOff);
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns last import by name.
	 * @return last import by name
	 */
	public int lastImp()
	{
		return lastImport;
	}
	
	/**
	 * Returns last import by location.
	 * @return last import by location
	 */
	public int lastImpLoc()
	{
		return lastImpLoc;
	}
	
	/**
	 * Closes the resources being used.
	 */
	public void close()
	{
		try {
			data.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
