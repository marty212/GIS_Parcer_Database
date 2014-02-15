import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;



/**
 * My command parser.
 * Basically reads in commands to output one at a time.
 * Only reads in the next command when asked.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class CommandParser {
	
	private Scanner          scan; //My scanner to read in stuff
	
	/**
	 * This sets up my command parser.
	 * @param commands the file the commands are in
	 */
	public CommandParser(File commands)
	{
		try {
			scan = new Scanner(commands);
		} catch (FileNotFoundException e) {
			System.err.println("Command file not found.  Program will exit!");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Reads next command in.
	 * @return the next command
	 */
	public Command nextCommand()
	{
		String s = scan.next(); //holds the thing I am comparing against other 
		if(s.equals("world"))
		{
			return new Command(Command.Comm.WORLD, scan.next(),
					scan.next(), scan.next(), scan.next());
		}
		else if(s.equals("import"))
		{
			return new Command(Command.Comm.IMPORT,
					scan.next(), "", "", "");
		}
		else if (s.equals("what_is_at"))
		{
			return new Command(Command.Comm.WHAT_IS_AT,
					scan.next(), scan.next(), "", "");
		}
		else if (s.equals("what_is"))
		{
			//Overriding the default pattern to make the name out.
			Pattern pattern = scan.delimiter(); //Store pattern to put back
			scan.useDelimiter("\\t[:alnum:]*");
			String s1 = scan.next(); //For return
			scan.useDelimiter(pattern);
			return new Command(Command.Comm.WHAT_IS,
					s1, scan.next(), "", "");
		}
		else if(s.equals("what_is_in"))
		{
			s = scan.next();
			if(!s.contains("-l") && !s.contains("-c"))
			{
				return new Command(Command.Comm.WHAT_IS_IN, s,
						scan.next(), scan.next(), scan.next());
			}
			else if(s.contains("-l"))
			{
				return new Command(Command.Comm.WHAT_IS_IN_L,
						scan.next(), scan.next(), scan.next(), scan.next());
			}
			else
			{
				return new Command(Command.Comm.WHAT_IS_IN_C,
						scan.next(), scan.next(), scan.next(), scan.next());
			}
		}
		else if (s.equals("debug"))
		{
			s = scan.next();
			if (s.equals("quad"))
			{
				return new Command(Command.Comm.DEBUG_QUAD);
			}
			else if (s.equals("hash"))
			{
				return new Command(Command.Comm.DEBUG_HASH);
			}
			else
			{
				return new Command(Command.Comm.DEBUG_POOL);
			}
		}
		else if (s.equals("quit"))
		{
			return new Command(Command.Comm.QUIT);
		}
		else
		{
			return new Command(Command.Comm.COMMENT, s + scan.nextLine(), "", "", "");
		}
	}
	/**
	 * Closing up the resources this is using.
	 */
	public void close()
	{
		scan.close();
	}
}
