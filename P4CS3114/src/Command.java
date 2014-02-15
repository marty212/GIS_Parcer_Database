
/**
 * Wraps the enumerated commands with their arguments.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class Command {
	/**
	 * The enumerations.
	 * @author Martin Borstad
	 * @project GIS System
	 * @version 04.16.2013
	 */
	public enum Comm {
	WORLD, IMPORT, WHAT_IS_AT, WHAT_IS, WHAT_IS_IN,
	WHAT_IS_IN_L, WHAT_IS_IN_C, DEBUG_QUAD, DEBUG_HASH, DEBUG_POOL,
	QUIT, COMMENT;
	}
	
	//All five vars are final so permission to change doesn't matter.
	final Comm c; //The command.
	final String one, two, three, four; //The arguments
	
	/**
	 * The arguments attached to command
	 * @param nC the command
	 * @param o  the first argument
	 * @param t  second argument
	 * @param th third argument
	 * @param f  fourth argument
	 */
	public Command(Comm nC, String o, String t, String th, String f)
	{
		c = nC;
		one = o;
		two = t;
		three = th;
		four = f;
	}
	/**
	 * No arguments.
	 * @param nC the command
	 */
	public Command(Comm nC)
	{
		c = nC;
		one = "";
		two = "";
		three = "";
		four = "";
	}
}
