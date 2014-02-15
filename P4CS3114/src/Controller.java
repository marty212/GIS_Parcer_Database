import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Date;
import java.util.Vector;

/**
 * Controls everything.
 * The driver uses the controller to make the project work.
 * First the constructor sets up the files.
 * Then parse commands handle commands.
 * Finally, the resources are closed.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class Controller
{
	private File                    database;    //My database file
	private File                    command;     //My command file
	private File                    log;         //My log file
	private FileParser              parseFile;   //My file parser
	private RandomAccessFile        l;           //My log file
	private CommandParser           parseCommand;//My command parser
	private prQuadtree<Point<Long>> tree;        //My tree
	private HashTable<HashEntry>    table;       //My hash table
	
	final static DateFormat format = 
			new SimpleDateFormat("HH:mm:ss MM/dd/yyyy"); 
    //used for format
	
	/**
	 * Constructs controller.
	 * @param nData the database name
	 * @param nComm the commands file name
	 * @param nLog  the log file name
	 */
	public Controller(String nData, String nComm, String nLog)
	{
		try
		{
			database = new File(nData);
			command  = new File(nComm);
			log      = new File(nLog);
			log.delete(); //If it is there, now it is not.
			table = new HashTable<HashEntry>();
			l = new RandomAccessFile(log, "rw");
			parseCommand = new CommandParser(command);
			Command c = parseCommand.nextCommand();//Make the world
			/**
			 * Print out the comments.
			 */
			while(c.c == Command.Comm.COMMENT)
			{
				l.writeBytes(c.one + "\n");
				c = parseCommand.nextCommand();
			}
			tree = new prQuadtree<Point<Long>>(
					Coordinates.convertToSec(c.one, true),
					Coordinates.convertToSec(c.two, true),
					Coordinates.convertToSec(c.three, false),
					Coordinates.convertToSec(c.four, false));
			l.writeBytes("world  " + c.one + " " + c.two
					+ "\t" + c.three + " " + c.four + "\n\nGIS Program\n\n");
			l.writeBytes("Author: Martin Borstad\n");
			l.writeBytes("dbFile:\t" + nData + "\nscript:\t" + nComm +
					"\nlog:\t" + nLog +	"\nStart time: "
					+ format.format(new Date()) + "\n");
			l.writeBytes("Quadtree children are printed in" +
					" the order SW  SE  NE  NW\n");
			l.writeBytes("----------------------------" +
					"--------------------------------\n\n");
			l.writeBytes("Latitude/longitude values in " +
					"index entries are shown as signed" +
					" integers, in total seconds.\n");
			l.writeBytes("\t\t  " + tree.yMax + "\n" +
					tree.xMin + "\t\t\t\t" + tree.xMax +
					"\n\t\t  " + tree.yMin + "\n\n");
			l.writeBytes("----------------------------------" +
					"--------------------------\n");
			parseFile = new FileParser(table, database, tree);
		}
		catch(Exception e)
		{
			System.err.print("Error.");
			System.exit(1);
			e.printStackTrace();
		}
	}
	
	/**
	 * Does the commands.
	 * @return commands done successfully
	 */
	public boolean parseCommands()
	{
		Stack<Buffer> stack = new Stack<Buffer>(); //The buffer pool
		int i = 1;
		/**
		 * Goes through all the commands.
		 */
		while(true)
		{
			try {
				Command c = parseCommand.nextCommand();
				//Pull up the next command
				if(c.c != Command.Comm.COMMENT)
					l.writeBytes("Command " + i++ + ":  ");
				//Lets start the output
				if (c.c == Command.Comm.DEBUG_HASH)
				{
					l.writeBytes("debug\thash\n");
					l.writeBytes("\n");
					table.printHash(l);
				}
				else if (c.c == Command.Comm.DEBUG_POOL)
				{
					l.writeBytes("debug\tpool\n\nMRU\n");
					/**
					 * To output stack correctly, I have to go backwards.
					 */
					for(int z = stack.size(); z > 0; z--)
					{
						l.writeBytes(stack.get(z - 1).toString());
					}
					l.writeBytes("LRU\n");
				}
				else if (c.c == Command.Comm.DEBUG_QUAD)
				{
					l.writeBytes("debug\tquad\n\n");
					tree.printTree(l);
					l.writeBytes("\n");
				}
				else if (c.c == Command.Comm.IMPORT)
				{
					l.writeBytes("import\t" + c.one + "\n\n");
					int a = parseFile.imp(new File(c.one));
					//Need to do this to set up rest of input.
					l.writeBytes("\nImported Features by name:   "
							+ parseFile.lastImp());
					l.writeBytes("\nLongest probe sequence:      "
							+ a);
					l.writeBytes("\nImported Locations:          "
							+ parseFile.lastImpLoc() + "\n");
				}
				else if (c.c == Command.Comm.QUIT)
				{
					l.writeBytes("quit\n\nTerminating execution" +
							" of commands.\nEnd Time: "
							+  format.format(new Date()) + "\n");
					break;
				}
				else if (c.c == Command.Comm.WHAT_IS_AT)
				{
					l.writeBytes("what_is_at\t" + c.one + " " + c.two +"\n\n");
					Point<Long> find = new Point<Long>(
							Coordinates.convertToSec(c.two, true),
							Coordinates.convertToSec(c.one, false)); 
					//I want to find this point.
					if(stack.contains(find))
					{
						Vector<Long> tr = tree.find(find).getV();
						//Lets make sure to hit every point.
						ArrayList<Buffer> buff = new ArrayList<Buffer>();
						//Made to store stuff to put back on the stack.
						/**
						 * While the stack has more of the things I want.
						 */
						 while(stack.contains(find))
						 {
							 buff.add(stack.remove(stack.lastIndexOf(find)));
						 }
						 /**
						  * Lets fix the stack and write the output.
						  */
						 for(Buffer b : buff)
						 {
							 stack.push(b);
							 tr.remove(b.offset);
							 l.writeBytes(b.offset + ": "
									 + b.record.report("'fN'  'cN'  'sNC'\n"));
						 }
						 /**
						  * If I have anything left, I want to go through it.
						  */
						 for(long z : tr)
						 {
							 Buffer rec = new Buffer(
									 parseFile.get(z), z);
							 //temp storage
							 l.writeBytes(z + ": "
									 + rec.record.report(
											 "'fN'  'cN'  'sAC'\n"));
							 stack.push(rec);
						 }
					}
					else
					{
						find = tree.find(find);
						if (find == null)
						{
							l.writeBytes("No elements found.\n");
						}
						else
						{
							/**
							 * For each offset, see if it is in the stack
							 * or get it from the database.
							 */
							for(Long off : find.getV())
							{
								Buffer rec;//temp storage
								if(stack.contains(new Buffer(null, off)))
								{
									rec = stack.get(stack.lastIndexOf(
											new Buffer(null, off)));
									stack.remove(stack.lastIndexOf(
											new Buffer(null, off)));
								}
								else
								{
									rec = new Buffer(parseFile.get(off), off);
								}
								l.writeBytes(off + ": " + rec.record.report(
										"'fN'  'cN'  'sAC'\n"));
								stack.push(rec);
							}
							/*
							 * Resize stack
							 */
							while(stack.size() > 20)
							{
								/*
								 * Remove first element in stack
								 */
								for(Buffer b : stack)
								{
									stack.remove(b);
									break;
								}
							}
						}
					}
				}
				/**
				 * what_is<tab><feature name><tab><state abbreviation>
				 */
				else if (c.c == Command.Comm.WHAT_IS)
				{
					l.writeBytes("what_is\t" + c.one + " " + c.two +"\n\n");
					HashEntry v = table.retrive(
							new HashEntry(c.one, c.two, 0));
					//Need this in 2 different spots.
					if (v != null)
					{
						/*
						 * Run through the elements I found.
						 */
						for(long z : v.getOffset())
						{
							Buffer rec;//Add this to my buffer pool
							           //and report on it.
							if(stack.contains(new Buffer(null, z)))
							{
								rec = stack.get(
										stack.lastIndexOf(
												new Buffer(null, z)));
								stack.remove(
										stack.lastIndexOf(
												new Buffer(null, z)));
							}
							else
							{
								rec = new Buffer(parseFile.get(z), z);
							}
							stack.push(rec);
							l.writeBytes(rec.offset + ": "
									+ rec.record.report("'cN'  'c1' 'c2'\n"));
							/*
							 * Resize stack
							 */
							while(stack.size() > 20)
							{
								/*
								 * Remove first element in stack
								 */
								for(Buffer b : stack)
								{
									stack.remove(b);
									break;
								}
							}
						}
					}
					else
					{
						l.writeBytes("No elements match "
								+ c.one + " and " + c.two + "\n");
					}
				}
				/**
				 * what_is_in<tab><geographic coordinate><tab><half-height><tab><half-width>
				 */
				else if (c.c == Command.Comm.WHAT_IS_IN)
				{
					l.writeBytes("what_is_in\t" + c.one + " "
							+ c.two + "\t" + c.three + "\t" + c.four + "\n\n");
					int x = Coordinates.convertToSec(c.two, true);//longitude
					int y = Coordinates.convertToSec(c.one, false);//latitude
					Vector<Point<Long>> v = tree.find(x - Integer.parseInt(c.four),
							x + Integer.parseInt(c.four),
							y - Integer.parseInt(c.three),
							y + Integer.parseInt(c.three));//Find all the elements
					int z = 0;//to count
					/*
					 * Count the number of elements in the vector.
					 */
					for(Point<Long> p : v)
					{
						/*
						 * Still counting.
						 */
						for(@SuppressWarnings("unused") Long off : p.getV())
						{
							z++;
						}
					}
					l.writeBytes("The following " + z +
							" features were found in ("
							+ c.one + " +/- " + c.three + ", " + c.one +
							" +/- " + c.four + ")\n");
					/*
					 * Check out all the offsets given.
					 */
					for(Point<Long> p : v)
					{
						if(stack.contains(p))
						{
							ArrayList<Buffer> sB =
									new ArrayList<Buffer>();
							//To readd to buffer later.
							/*
							 * Run through all the offsets.
							 */
							for(Long off : p.getV())
							{
								Buffer b;//To add to buffer.
								if (stack.lastIndexOf(p) != -1)
								{
									b = stack.get(stack.lastIndexOf(p));
									if(off != b.offset)
									{
										b = new Buffer(
												parseFile.get(off), off);
									}
									else
									{
										stack.remove(stack.lastIndexOf(p));
									}
								}
								else
								{
									b = new Buffer(parseFile.get(off), off);
								}
								sB.add(b);
								l.writeBytes(b.offset + ": "
										+ b.record.report("'fN'  'cN'  'c1' 'c2'\n"));
							}
							/*
							 * Readd it to stack.
							 */
							for(Buffer b : sB)
							{
								stack.push(b);
							}
						}
						else
						{
							/*
							 * Not in stack, add regularly.
							 */
							for(Long off : p.getV())
							{
								Buffer b = new Buffer(parseFile.get(off), off);
								//Make buffer to add to buffer pool.
								stack.push(b);
								l.writeBytes(b.offset + ": "
										+ b.record.report(
												"'fN'  'cN'  'c1' 'c2'\n"));
							}
						}
					}
					/*
					 * Resize stack
					 */
					while(stack.size() > 20)
					{
						/*
						 * Remove first element in stack
						 */
						for(Buffer b : stack)
						{
							stack.remove(b);
							break;
						}
					}
				}
				else if (c.c == Command.Comm.WHAT_IS_IN_L)
				{
					l.writeBytes("what_is_in\t-l\t" + c.one + " "
							+ c.two + "\t" + c.three + "\t" + c.four + "\n\n");
					int x = Coordinates.convertToSec(c.two, true);//longitude
					int y = Coordinates.convertToSec(c.one, false);//latitude
					Vector<Point<Long>> v = tree.find(
							x - Integer.parseInt(c.four),
							x + Integer.parseInt(c.four),
							y - Integer.parseInt(c.three),
							y + Integer.parseInt(c.three));//Find the elements
					int z = 0;//to count
					/*
					 * Lets count
					 */
					for(Point<Long> p : v)
					{
						/*
						 * Counting
						 */
						for(@SuppressWarnings("unused")Long off : p.getV())
						{
							z++;
						}
					}
					l.writeBytes("The following " + z +
							" features were found in ("
							+ c.one + " +/- " + c.three + ", " + c.one +
							" +/- " + c.four + ")\n");
					/*
					 * Run through all the offsets.
					 */
					for(Point<Long> p : v)
					{
						if(stack.contains(p))
						{
							ArrayList<Buffer> sB = new ArrayList<Buffer>();
							//For buffer
							/*
							 * Lets run through all the offsets.
							 */
							for(Long off : p.getV())
							{
								Buffer b;//To add to buffer pool
								if (stack.lastIndexOf(p) != -1)
								{
									b = stack.get(stack.lastIndexOf(p));
									if(off != b.offset)
									{
										b = new Buffer(
												parseFile.get(off), off);
									}
									else
									{
										stack.remove(
												stack.lastIndexOf(p));
									}
								}
								else
								{
									b = new Buffer(
											parseFile.get(off), off);
								}
								sB.add(b);
								l.writeBytes(b.record.reportRel() + "\n");
							}
							/*
							 * Add it to buffer pool
							 */
							for(Buffer b : sB)
							{
								stack.push(b);
							}
						}
						else
						{
							/*
							 * Not in buffer pool, lets get all the offsets
							 * from for these records from file.
							 */
							for(Long off : p.getV())
							{
								Buffer b = new Buffer(parseFile.get(off), off);
								stack.push(b);
								l.writeBytes(b.record.reportRel() + "\n");
							}
						}
					}
					/*
					 * Resize stack
					 */
					while(stack.size() > 20)
					{
						/*
						 * Remove first element in stack
						 */
						for(Buffer b : stack)
						{
							stack.remove(b);
							break;
						}
					}
				}
				else if (c.c == Command.Comm.WHAT_IS_IN_C)
				{
					l.writeBytes("what_is_in\t-c\t" + c.one +
							" " + c.two + "\t" + c.three + "\t"
							+ c.four + "\n\n");
					int x = Coordinates.convertToSec(c.two, true);//longitude
					int y = Coordinates.convertToSec(c.one, false);//latitude
					Vector<Point<Long>> v = tree.find(
							x - Integer.parseInt(c.four),
							x + Integer.parseInt(c.four),
							y - Integer.parseInt(c.three),
							y + Integer.parseInt(c.three));
					//Find elements in tree
					int z = 0;//Lets count
					/*
					 * Need to count
					 */
					for(Point<Long> p : v)
					{
						/*
						 * Counting
						 */
						for(@SuppressWarnings("unused") Long off : p.getV())
						{
							//Counting the amount of offsets.
							z++;
						}
					}
					l.writeBytes(z + " features were found in ("
							+ c.one + " +/- " + c.three + ", " + c.one +
							" +/- " + c.four + ")\n");
				}
				else
				{
					l.writeBytes(c.one + "\n");
					continue;
				}
				l.writeBytes("--------------------------" +
						"----------------------------------\n");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		try {
			l.writeBytes("---------------------------------" +
					"---------------------------");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Close up the files.
	 * @return was it successful
	 */
	public boolean close()
	{
		try {
			l.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parseFile.close();
		parseCommand.close();
		return true;
	}
	
	/**
	 * My buffer class.
	 * @author Martin Borstad
	 * @project GIS System
	 * @version 04.16.2013
	 */
	public class Buffer
	{
		final GISRecord record;//The record
		final long      offset;//The offset
		
		/**
		 * Makes a new buffer
		 * @param rec the record stored here
		 * @param off the offset at which it is stored at
		 */
		public Buffer(GISRecord rec, long off)
		{
			record = rec;
			offset = off;
		}
		
		/**
		 * Prints out the buffer object.
		 * @return a readable string.
		 */
		public String toString()
		{
			String s = String.valueOf(offset);//For return
			while(s.length() < 6)
			{
				s = " " + s;
			}
			return s + ":  " + record.report() + "\n";
		}
		
		/**
		 * Sees if this buffer is equal to anything else.
		 * Is based on what the other class is.
		 * @return is it equal
		 */
		public boolean equals(Object o)
		{
			if(o instanceof Point<?>)
			{
				return ((Point<?>)o).equals(
						new Point<Long>(record.getLongS(),
								record.getLongS()));
			}
			else if (o instanceof HashEntry)
			{
				return ((HashEntry)o).equals(
						new HashEntry(record.report("'fN'"),
								record.report("'sAC'"), 0));
			}
			else if (o instanceof Controller.Buffer)
			{
				return ((Controller.Buffer)o).offset == offset;
			}
			return false;
		}
		
	}
}
