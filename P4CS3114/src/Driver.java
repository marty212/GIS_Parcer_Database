
/**
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 * 
 * This program is my solution to the major project.
 * It uses a stack as the buffer pool.
 * It also uses vectors to pass multiple objects at once.
 * The driver calls control and control processes the commands
 * using other classes.
 * 
 * Call with this sequence:
 * Database command log
 */
public class Driver {

	/**
	 * This runs the program.
	 * @param args database.txt command.txt log.txt
	 */
	public static void main(String args[])
	{
		if (args.length < 3)
		{
			System.err.println("Not enough inputs.  Program exiting.");
			return;
		}
		Controller c = new Controller(args[0], args[1], args[2]);
		c.parseCommands();
		c.close();
	}
}
//On my honor:
//
//- I have not discussed the Java language code in my program with
//anyone other than my instructor or the teaching assistants
//assigned to this course.
//
//- I have not used Java language code obtained from another student,
//or any other unauthorized source, either modified or unmodified.
//
//- If any Java language code or documentation used in my program
//was obtained from another source, such as a text book or course
//notes, that has been clearly noted with a proper citation in
//the comments of my program.
//
//- I have not designed this program in such a way as to defeat or
//interfere with the normal operation of the Automated Grader.
//
//Pledge: On my honor, I have neither given nor received unauthorized
//aid on this assignment.
//
//Martin Borstad