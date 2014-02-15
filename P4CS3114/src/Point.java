import java.util.Vector;


/**
 * The point class for the quadtree.
 * Implements the majority of the functions.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class Point<T> implements Compare2D<Point<T>> {

	private long      xcoord;
	private long      ycoord;
	private Vector<T> v;     //The elements stored here.

	/**
	 * Makes a blank point class.
	 * @param i the initial value stored at point
	 */
	public Point(T i) {
		xcoord = 0;
		ycoord = 0;
		v = new Vector<T>();
		v.add(i);
	}

	/**
	 * Makes a point class.
	 * @param x the x coord
	 * @param y the y coord
	 */
	public Point(long x, long y) {
		xcoord = x;
		ycoord = y;
		v = new Vector<T>();
	}

	/**
	 * Returns the X coord.
	 * @return the xcoord
	 */
	public long getX() {
		return xcoord;
	}

	/**
	 * Gets the Y coord.
	 * @return the ycoord
	 */
	public long getY() {
		return ycoord;
	}

	/**
	 * Returns the direction from the given point.
	 * @param X the x coordinate
	 * @param Y the y coordinate
	 * @return the direction it is
	 */
	public Direction directionFrom(long X, long Y) { 
		if (xcoord == X && ycoord == Y)
		{
			return Direction.NOQUADRANT;
		}
		else if (xcoord >= X)
		{
			if (xcoord == X)
			{
				return ycoord > Y ? Direction.NW : Direction.SE;
			}
			return ycoord > Y ? Direction.NE : Direction.SE;
		}
		else
		{
			return ycoord > Y ? Direction.NW : Direction.SW;
		}
	}

	/**
	 * Sees what quadrant this point is in based on the given region.
	 * @param xLo the x low
	 * @param xHi the x high
	 * @param yLo the y low
	 * @param yHi the y high
	 * @return what direction is it
	 */
	public Direction inQuadrant(double xLo, double xHi, 
			double yLo, double yHi) {
		if (xcoord <= xHi && xcoord >= (xHi + xLo)/2)
		{
			if (ycoord <= yHi && ycoord >= (yHi + yLo)/2)
			{
				return xcoord == (xHi + xLo)/2  && ycoord != 0 ? Direction.NW : Direction.NE;
			}
			else if (ycoord >= yLo && ycoord < (yHi + yLo)/2)
			{
				return Direction.SE;
			}
		}
		else if (xcoord >= xLo && xcoord < (xHi + xLo)/2)
		{
			if (ycoord <= yHi && ycoord > (yHi + yLo)/2)
			{
				return Direction.NW;
			}
			else if (ycoord >= yLo && ycoord <= (yHi + yLo)/2)
			{
				return Direction.SW;
			}
		}
		return Direction.NOQUADRANT;
	}

	/**
	 * Sees if the coord is in the given region.
	 * @param xLo the low x
	 * @param xHi the high x
	 * @param yLo the low y
	 * @param yhi the high y
	 * @return whether it is in the region or not
	 */
	public boolean   inBox(double xLo, double xHi, 
			double yLo, double yHi) { 
		return xcoord >= xLo && xcoord <= xHi && ycoord >= yLo && ycoord <= yHi;
	}

	/**
	 * Provides a toString.
	 * @return a coordinate based output.
	 */
	public String toString() {
		StringBuilder s = new StringBuilder("[(" + xcoord + ", " + ycoord + ")");
		for (T i : v)
		{
			s.append(", ");
			s.append(i);
		}
		s.append("]");
		return s.toString();
	}

	/**
	 * Returns offsets.
	 * @return offsets
	 */
	@SuppressWarnings("unchecked")
	public Vector<T> getV()
	{
		return (Vector<T>)v.clone();
	}

	/**
	 * Sees if o is a Point then compares.
	 * @param o the object being compared to
	 * @return whether they are equal
	 */
	public boolean equals(Object o) { 
		if (o instanceof Point)
		{
			Point<?> test = (Point<?>)o;//To avoid having to cast multiple times
			if (test.xcoord == xcoord && test.ycoord == ycoord)
			{
				return true;
			}
		}
		else if (o instanceof Controller.Buffer)
		{
			Controller.Buffer buff = (Controller.Buffer)o;
			return v.contains(buff.offset);
		}
		return false;
	}

	/**
	 * Remove an element.
	 * @param i the element being removed
	 * @return was it successful?
	 */
	public boolean remove(T i)
	{
		return v.remove(i);
	}

	/**
	 * Sees if the element is in here.
	 * @param i the element being compared
	 * @return is it in here?
	 */
	public boolean contains(T i)
	{
		return v.contains(i);
	}

	/**
	 * Gets the size of the vector.
	 * @return the size.
	 */
	public int size()
	{
		return v.size();
	}

	/**
	 * Adds element to vector.
	 * @param i the element being added
	 * @return was it successful?
	 */
	public boolean add(T i)
	{
		return !v.contains(i) ? v.add(i) : false;
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