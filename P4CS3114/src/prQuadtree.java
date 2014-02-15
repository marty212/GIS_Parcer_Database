import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

// The test harness will belong to the following package; the quadtree
// implementation must belong to it as well.  In addition, the quadtree
// implementation must specify package access for the node types and tree
// members so that the test harness may have access to it.
//

/**
 * Implements the quadtree.
 * The helper methods employ the use of the center of the
 * given region and the length it can go to reach the sides.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 * @param <T>
 */
public class prQuadtree<T extends Compare2D<? super T>> {

	// You must use a hierarchy of node types with an abstract base
	// class. You may use different names for the node types if
	// you like (change displayHelper() accordingly).
	abstract class prQuadNode {
	}

	class prQuadLeaf extends prQuadNode {
		Vector<T> Elements;
	}

	class prQuadInternal extends prQuadNode {
		prQuadNode NW, NE, SE, SW;
	}

	private prQuadNode root;
	final long xMin, xMax, yMin, yMax;
	final int BUCKET_SIZE = 4;

	// Initialize quadtree to empty state, representing the specified region.
	public prQuadtree(long xMin, long xMax, long yMin, long yMax) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
	}

	// Pre: elem != null
	// Post: If elem lies within the tree's region, and elem is not already
	// present in the tree, elem has been inserted into the tree.
	// Return true iff elem is inserted into the tree.
	public boolean insert(T elem) {
		if (!elem.inBox(xMin, xMax, yMin, yMax)) {
			return false;
		}
		prQuadNode node = insertHelper(elem, root, (xMax + xMin) / 2,
				(yMax + yMin) / 2, Math.abs((xMax - xMin) / 4),
				Math.abs((yMax - yMin) / 4));
		root = node == null ? root : node;
		return node != null;
	}

	/**
	 * This method is the recursive helper for insert. Basically, it sees if a
	 * leaf is where it is at then makes a new internal node for both. The
	 * newInternal covers the logic of inserting a new internal node. Because I
	 * check to make sure the point is in the region, I can treat the region as
	 * a square regardless of the actual shape.
	 * 
	 * @param elem element we are adding
	 * @param node the node we are at
	 * @param x    the x center of the node
	 * @param y    the y center of the node
	 * @param lengthX how much can I go on X
	 * @param legnthY how much can I go on Y
	 * @return the new node or null if it was already there
	 */
	@SuppressWarnings("unchecked")
	//Checked through logic
	private prQuadNode insertHelper(T elem, prQuadNode node, double x,
			double y, double lengthX, double lengthY)
	{
		if (node instanceof prQuadtree.prQuadLeaf)
		{
			if (((prQuadLeaf)node).Elements.contains(elem))
			{
				return null;
			}
			else
			{
				if (((prQuadLeaf)node).Elements.size() < BUCKET_SIZE)
				{
					((prQuadLeaf)node).Elements.add(elem);
				}
				else
				{
					node = newInternal((prQuadLeaf)node, newLeaf(elem), x, y,
							lengthX, lengthY);
				}
				return node;
			}
		}
		if (node == null)
		{
			return newLeaf(elem);
		}
		prQuadNode nNode = null; //for return
		switch(elem.inQuadrant(x - 2 * lengthX, x + 2 * lengthX, y - 2
				* lengthY, y + 2 * lengthY))
				{
				case NE:
					
					nNode = insertHelper(elem, ((prQuadInternal) node).NE
							, x + lengthX, y + lengthY, lengthX / 2,
							lengthY / 2);
					((prQuadInternal) node).NE = nNode != null ? nNode
							: ((prQuadInternal) node).NE;
					break;
				case NW:
					nNode = insertHelper(elem, ((prQuadInternal) node).NW,
							x - lengthX, y + lengthY,
							lengthX / 2, lengthY / 2);
					((prQuadInternal) node).NW = nNode != null ? nNode
							: ((prQuadInternal) node).NW;
					break;
				case SE:
					nNode = insertHelper(elem, ((prQuadInternal) node).SE,
							x + lengthX, y - lengthY, lengthX / 2,
							lengthY / 2);
					((prQuadInternal) node).SE = nNode != null ? nNode
							: ((prQuadInternal) node).SE;
					break;
				case SW:
					nNode = insertHelper(elem, ((prQuadInternal) node).SW,
							x - lengthX, y - lengthY, lengthX / 2,
							lengthY / 2);
					((prQuadInternal) node).SW = nNode != null ? nNode
							: ((prQuadInternal) node).SW;
					break;
				default:
					break;
				}
		return nNode == null ? nNode : node;
	}

	/**
	 * Makes a new leaf. Self-explanatory.
	 * 
	 * @param elem the element of the leaf
	 * @return the newly made leaf
	 */
	private prQuadLeaf newLeaf(T elem) {
		prQuadLeaf ret = new prQuadLeaf(); // The return
		ret.Elements = new Vector<T>(BUCKET_SIZE);
		ret.Elements.add(elem);
		return ret;
	}

	/**
	 * Handles making a new internal node. Basically keeps making internal
	 * nodes until the two leafs are in different positions.
	 * 
	 * @param leaf1 the first leaf
	 * @param leaf2 the second leaf
	 * @param x     the x center of the internal node
	 * @param y     the y center of the internal node
	 * @param lengthX how much in x can I move
	 * @param lengthY how much in y can I move
	 * @return the newly made internal node with the leafs in it
	 */
	@SuppressWarnings("unchecked")
	private prQuadInternal newInternal(prQuadLeaf leaf1, prQuadLeaf leaf2,
			double x, double y, double lengthX, double lengthY) {
		prQuadInternal ret = new prQuadInternal(); //The new internal
		Vector<Direction> v = new Vector<Direction>(BUCKET_SIZE + 1);
		for (T elem : leaf1.Elements)
		{
			v.add(elem.inQuadrant(x - 2 * lengthX,
				x + 2 * lengthX, y - 2 * lengthY, y + 2 * lengthY));
		}
		v.add(leaf2.Elements.get(0).inQuadrant(x - 2 * lengthX,
				x + 2 * lengthX, y - 2 * lengthY, y + 2 * lengthY));
		int ne = 0,
			nw = 0,
			se = 0,
			sw = 0;
		for (Direction dir : v)
		{
			switch(dir)
			{
			case NE:
				ne++;
				break;
			case NW:
				nw++;
				break;
			case SE:
				se++;
				break;
			case SW:
				sw++;
				break;
			default:
				break;
			}
		}
		if (nw > BUCKET_SIZE)
		{
			ret.NW = newInternal(leaf1, leaf2, x - lengthX, y + lengthY,
					lengthX / 2, lengthY / 2);
		}
		else if(ne > BUCKET_SIZE)
		{
			ret.NE = newInternal(leaf1, leaf2, x + lengthX, y + lengthY,
					lengthX / 2, lengthY / 2);
		}
		else if(sw > BUCKET_SIZE)
		{
			ret.SW = newInternal(leaf1, leaf2, x - lengthX, y - lengthY,
					lengthX / 2, lengthY / 2);
		}
		else if(se > BUCKET_SIZE)
		{
			ret.SE = newInternal(leaf1, leaf2, x + lengthX, y - lengthY,
					lengthX / 2, lengthY / 2);
		}
		else
		{
			for (int i = 0; i <= BUCKET_SIZE ; i++)
			{
				T elem = i != BUCKET_SIZE ? leaf1.Elements.get(i) : leaf2.Elements.get(0);
				Direction dir = v.get(i);
				switch(dir)
				{
				case NE:
					if (ret.NE == null)
					{
						ret.NE = newLeaf(elem);
					}
					else
					{
						((prQuadtree<T>.prQuadLeaf)ret.NE).Elements.add(elem);
					}
					break;
				case NW:
					if (ret.NW == null)
					{
						ret.NW = newLeaf(elem);
					}
					else
					{
						((prQuadtree<T>.prQuadLeaf)ret.NW).Elements.add(elem);
					}
					break;
				case SE:
					if (ret.SE == null)
					{
						ret.SE = newLeaf(elem);
					}
					else
					{
						((prQuadtree<T>.prQuadLeaf)ret.SE).Elements.add(elem);
					}
					break;
				case SW:
					if (ret.SW == null)
					{
						ret.SW = newLeaf(elem);
					}
					else
					{
						((prQuadtree<T>.prQuadLeaf)ret.SW).Elements.add(elem);
					}
					break;
				default:
					break;

				}
			}
		}
		return ret;
	}

	// Pre: elem != null
	// Post: If elem lies in the tree's region, and a matching element occurs
	// in the tree, then that element has been removed.
	// Returns true iff a matching element has been removed from the tree.
	@SuppressWarnings("unchecked") //Checked logically
	public boolean delete(T Elem) {

		//Divide by 4 to get the actual width.
		//This makes math nicer later.
		prQuadNode node = deleteHelper(Elem, root, (xMax + xMin) / 2,
				(yMax + yMin) / 2, Math.abs((xMax - xMin) / 4),
				Math.abs((yMax - yMin) / 4));
		if (node instanceof prQuadtree.prQuadLeaf &&
				((prQuadLeaf)node).Elements.get(0) == null)
		{
			return false;
		}
		root = node;
		return true;
	}

	/**
	 * This the helper for delete. Basically goes down the quadtree till it
	 * finds the element then makes it null. Then the tree goes back up until it
	 * no longer has internal nodes that are filled with null.
	 * 
	 * @param Elem The Elem we are deleting
	 * @param node The node we are on
	 * @param x    The x-center
	 * @param y    The y-center
	 * @param lengthX How much x we got
	 * @param lengthY How much y we got
	 * @return the new node or a leaf with null element for not found
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	//Checked logically
	private prQuadNode deleteHelper(T elem, prQuadNode node, double x,
			double y, double lengthX, double lengthY) {
		if (node == null || (node instanceof prQuadtree.prQuadLeaf &&
				!((prQuadLeaf)node).Elements.get(0).equals(elem)))
		{
			return newLeaf(null);
		}
		else if (node instanceof prQuadtree.prQuadLeaf)
		{
			((prQuadtree.prQuadLeaf)node).Elements.remove(elem);
			if (((prQuadtree.prQuadLeaf)node).Elements.size() == 0)
			{
				return null;
			}
			return node;
		}
		prQuadInternal nNode = (prQuadInternal)node; //Casting is annoying
		prQuadNode     wNode = null; //My working node
		switch (elem.inQuadrant(x - 2 * lengthX, x + 2 * lengthX, y - 2
				   * lengthY, y + 2 * lengthY))
		{
		case NE:
			wNode = deleteHelper(elem, ((prQuadInternal) node).NE,
					x + lengthX, y + lengthY, lengthX / 2,
					lengthY / 2);
			if (wNode instanceof prQuadtree.prQuadLeaf &&
					((prQuadLeaf)wNode).Elements.get(0) == null)
			{
				return wNode;
			}
			nNode.NE = wNode;
			break;
		case NW:
			wNode = deleteHelper(elem, ((prQuadInternal) node).NW,
					x - lengthX, y + lengthY, lengthX / 2,
					lengthY / 2);
			if (wNode instanceof prQuadtree.prQuadLeaf &&
					((prQuadLeaf)wNode).Elements.get(0) == null)
			{
				return wNode;
			}
			nNode.NW = wNode;
			break;
		case SE:
			wNode = deleteHelper(elem, ((prQuadInternal) node).SE,
					x + lengthX, y - lengthY, lengthX / 2,
					lengthY / 2);
			if (wNode instanceof prQuadtree.prQuadLeaf &&
					((prQuadLeaf)wNode).Elements.get(0) == null)
			{
				return wNode;
			}
			nNode.SE = wNode;
			break;
		case SW:
			wNode = deleteHelper(elem, ((prQuadInternal) node).SW,
					x - lengthX, y - lengthY, lengthX / 2,
					lengthY / 2);
			if (wNode instanceof prQuadtree.prQuadLeaf &&
					((prQuadLeaf)wNode).Elements.get(0) == null)
			{
				return wNode;
			}
			nNode.SW = wNode;
			break;
		default:
			break;		
		}
		int count = 0; //Counting up the number of children
		if (nNode.NE != null)
		{
			count++;
		}
		if (nNode.NW != null)
		{
			count++;
		}
		if (nNode.SE != null)
		{
			count++;
		}
		if (nNode.SW != null)
		{
			count++;
		}
		if (count == 0)
		{
			return null;
		}
		else if (count == 1)
		{
			if (nNode.NE != null && !(nNode.NE instanceof
					prQuadtree.prQuadInternal))
			{
				return nNode.NE;
			}
			if (nNode.NW != null && !(nNode.NW instanceof
					prQuadtree.prQuadInternal))
			{
				return nNode.NW;
			}
			if (nNode.SE != null && !(nNode.SE instanceof
					prQuadtree.prQuadInternal))
			{
				return nNode.SE;
			}
			if (nNode.SW != null && !(nNode.SW instanceof
					prQuadtree.prQuadInternal))
			{
				return nNode.SW;
			}
			
		}
		return nNode;
	}

	// Pre: elem != null
	// Returns reference to an element x within the tree such that
	// elem.equals(x)is true, provided such a matching element occurs within
	// the tree; returns null otherwise.
	public T find(T Elem) {
		return findHelper(Elem, root, (xMax + xMin) / 2, (yMax + yMin) / 2,
				Math.abs((xMax - xMin) / 4), Math.abs((yMax - yMin) / 4));
	}

	/**
	 * The basic find helper. Traverses the tree like it is inserting.
	 * @param Elem What do we want to find
	 * @param node What node are we on
	 * @param x    What is the x center
	 * @param y    What is the y center
	 * @param lengthX How much x we have
	 * @param lengthY How much y we have
	 * @return the Elem if found otherwise null
	 */
	@SuppressWarnings("unchecked")
	// Logic dictates the checking of the type of nodes
	private T findHelper(T Elem, prQuadNode node, double x, double y,
			double lengthX, double lengthY) {
		if (node == null)
		{
			return null;
		} else if(node instanceof prQuadtree.prQuadLeaf) {
			for (T i : ((prQuadLeaf)node).Elements)
			{
				if(i.equals(Elem))
				{
					return i;
				}
			}
			return null;
		} else {
			switch (Elem.inQuadrant(x - 2 * lengthX, x + 2 * lengthX, y -
					2 * lengthY, y + 2 * lengthY)) {
			case NE:
				return findHelper(Elem, ((prQuadInternal)node).NE, x + lengthX,
						y + lengthY, lengthX / 2, lengthY / 2);
			case NW:
				return findHelper(Elem, ((prQuadInternal)node).NW, x - lengthX,
						y + lengthY, lengthX / 2, lengthY / 2);
			case SE:
				return findHelper(Elem, ((prQuadInternal)node).SE, x + lengthX,
						y - lengthY, lengthX / 2, lengthY / 2);
			case SW:
				return findHelper(Elem, ((prQuadInternal)node).SW, x - lengthX,
						y - lengthY, lengthX / 2, lengthY / 2);
			default: // Shouldn't happen
				break;
			}
		}
		return null;// Well, something went wrong
	}

	// Pre: xLo, xHi, yLo and yHi define a rectangular region
	// Returns a collection of (references to) all elements x such that x is
	// in the tree and x lies at coordinates within the defined rectangular
	// region, including the boundary of the region.
	public Vector<T> find(long xLo, long xHi, long yLo, long yHi) {
		return findHelper(root, xLo, xHi, yLo, yHi, new Vector<T>(),
				(xMin + xMax) / 2, (yMin + yMax) / 2,
				Math.abs((xMax - xMin) / 4), Math.abs((yMax - yMin) / 4));
	}

	/**
	 * Helps the overloaded find do its thing.
	 * 
	 * @param node the node we are at
	 * @param xLo the defined rectangle
	 * @param xHi the defined rectangle
	 * @param yLo the defined rectangle
	 * @param yHi the defined rectangle
	 * @param v   the vector being returned
	 * @param x   the x center
	 * @param y   the y center
	 * @param lengthX how far we can move in x
	 * @param lengthY how far we can move in y
	 * @return v
	 */
	@SuppressWarnings("unchecked")
	// Checked through logic
	private Vector<T> findHelper(prQuadNode node, long xLo, long xHi, long yLo,
			long yHi, Vector<T> v, double x, double y, double lengthX,
			double lengthY) {
		if (node instanceof prQuadtree.prQuadLeaf) {
			prQuadLeaf wNode = (prQuadLeaf) node; // casting is annoying
			//Is my point in the given region?
			for(T elem : wNode.Elements)
			{
				if (elem.inBox(xLo, xHi, yLo, yHi)) {
					v.add(elem);
				}
			}
			return v;
		}
		prQuadInternal wNode = (prQuadInternal) node;// casting is annoying
		if (yLo > y && xHi < x)// Above and to the left
		{
			if (wNode.NW != null) {
				findHelper(wNode.NW, xLo, xHi, yLo, yHi, v, x - lengthX, y
						+ lengthY, lengthX / 2, lengthY / 2);
			}
		} else if (yLo > y && xLo > x)// Above to the right
		{
			if (wNode.NE != null) {
				findHelper(wNode.NE, xLo, xHi, yLo, yHi, v, x + lengthX, y
						+ lengthY, lengthX / 2, lengthY / 2);
			}
		} else if (yHi < y && xHi < x)// Below to the left
		{
			if (wNode.SW != null) {
				findHelper(wNode.SW, xLo, xHi, yLo, yHi, v, x - lengthX, y
						- lengthY, lengthX / 2, lengthY / 2);
			}
		} else if (yHi < y && xLo > x)// Below to the right
		{
			if (wNode.SE != null) {
				findHelper(wNode.SE, xLo, xHi, yLo, yHi, v, x + lengthX, y
						- lengthY, lengthX / 2, lengthY / 2);
			}
		}
		else if (x > xHi) //left
		{
			if (wNode.SW != null) {
				findHelper(wNode.SW, xLo, xHi, yLo, yHi, v, x - lengthX, y
						- lengthY, lengthX / 2, lengthY / 2);
			}
			if (wNode.NW != null) {
				findHelper(wNode.NW, xLo, xHi, yLo, yHi, v, x - lengthX, y
						+ lengthY, lengthX / 2, lengthY / 2);
			}
		}
		else if (x < xLo) //right
		{
			if (wNode.NE != null) {
				findHelper(wNode.NE, xLo, xHi, yLo, yHi, v, x + lengthX, y
						+ lengthY, lengthX / 2, lengthY / 2);
			}
			if (wNode.SE != null) {
				findHelper(wNode.SE, xLo, xHi, yLo, yHi, v, x + lengthX, y
						- lengthY, lengthX / 2, lengthY / 2);
			}
		}
		else if (y < yLo)//above
		{
			if (wNode.NE != null) {
				findHelper(wNode.NE, xLo, xHi, yLo, yHi, v, x + lengthX, y
						+ lengthY, lengthX / 2, lengthY / 2);
			}
			if (wNode.NW != null) {
				findHelper(wNode.NW, xLo, xHi, yLo, yHi, v, x - lengthX, y
						+ lengthY, lengthX / 2, lengthY / 2);
			}
		}
		else if(y > yHi)//below
		{
			if (wNode.SE != null) {
				findHelper(wNode.SE, xLo, xHi, yLo, yHi, v, x + lengthX, y
						- lengthY, lengthX / 2, lengthY / 2);
			}
			if (wNode.SW != null) {
				findHelper(wNode.SW, xLo, xHi, yLo, yHi, v, x - lengthX, y
						- lengthY, lengthX / 2, lengthY / 2);
			}
		} else { //Oh noes, my point is inside the given region
			if (wNode.NW != null) {
				findHelper(wNode.NW, xLo, xHi, yLo, yHi, v, x - lengthX, y
						+ lengthY, lengthX / 2, lengthY / 2);
			}
			if (wNode.NE != null) {
				findHelper(wNode.NE, xLo, xHi, yLo, yHi, v, x + lengthX, y
						+ lengthY, lengthX / 2, lengthY / 2);
			}
			if (wNode.SW != null) {
				findHelper(wNode.SW, xLo, xHi, yLo, yHi, v, x - lengthX, y
						- lengthY, lengthX / 2, lengthY / 2);
			}
			if (wNode.SE != null) {
				findHelper(wNode.SE, xLo, xHi, yLo, yHi, v, x + lengthX, y
						- lengthY, lengthX / 2, lengthY / 2);
			}
		}
		return v;
	}
	
	/**
	 * Makes it easy to print trees.
	 * @param log the log we are writing to
	 */
	public void printTree(RandomAccessFile log)
	{
		printTreeHelper(log, root, "");
	}
	/**
	 * From course notes.
	 * @param sRoot the root we are on
	 * @param Padding the current padding
	 */
	@SuppressWarnings("unchecked")
	public void	printTreeHelper(prQuadNode sRoot, String Padding, RandomAccessFile log) {

		try {
			// Check for empty leaf
			if( sRoot == null )
			{
				log.writeBytes(Padding + "*");
				return;
			}
			// Check for and process SW and SE subtrees
			if( sRoot.getClass().getName().equals("prQuadtree$prQuadInternal") )
			{
				prQuadInternal p = (prQuadInternal) sRoot;
				printTreeHelper(p.SW, Padding + " ", log);
				printTreeHelper(p.SE, Padding + " ", log);
			}
			// Display indentation padding for current node
			log.writeBytes(Padding + "\n");
			// Determine if at leaf or internal and display accordingly
			if( sRoot.getClass().getName().equals("prQuadtree$prQuadLeaf") )
			{
				prQuadLeaf p = (prQuadLeaf) sRoot;
				log.writeBytes(Padding);
				for (T i : p.Elements)
				{
					log.writeBytes(i.toString() + " ");
				}
				log.writeBytes("\n");
			}
			else
				log.writeBytes( Padding + "@\n" );
			// Check for and process NE and NW subtrees
			if( sRoot.getClass().getName().equals("prQuadtree$prQuadInternal") )
			{
				prQuadInternal p = (prQuadInternal) sRoot;
				printTreeHelper(p.NE, Padding + " ", log);
				printTreeHelper(p.NW, Padding + " ", log);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Modified from the Lewis file.
	 * It makes a nicer tree than the method above.
	 * @param Out The log we are writing to
	 * @param sRoot the root we are on
	 * @param Padding the padding we are using for this node
	 */
	@SuppressWarnings("unchecked")
	public void printTreeHelper(RandomAccessFile Out, prQuadNode sRoot, String Padding) {
		String pad = "---";
	      try {
	         // Check for empty leaf
	         if ( sRoot == null ) {
	            Out.writeBytes( " " + Padding + "*\n");
	            return;
	         }
	         // Check for and process SW and SE subtrees
	         if ( sRoot.getClass().getName().equals("prQuadtree$prQuadInternal") ) {
	            prQuadInternal p = (prQuadInternal) sRoot;
	            printTreeHelper(Out, p.SW, Padding + pad);
	            printTreeHelper(Out, p.SE, Padding + pad);
	         }
	 
	         // Determine if at leaf or internal and display accordingly
	         if ( sRoot.getClass().getName().equals("prQuadtree$prQuadLeaf") ) {
	            prQuadLeaf p = (prQuadLeaf) sRoot;
	            Out.writeBytes(Padding);
	            for (int pos = 0; pos < p.Elements.size(); pos++) {
	               Out.writeBytes(p.Elements.get(pos) + "  " );
	            }
	            Out.writeBytes("\n");
	         }
	         else if ( sRoot.getClass().getName().equals("prQuadtree$prQuadInternal") )
	            Out.writeBytes( Padding + "@\n" );
	         else
	            Out.writeBytes( sRoot.getClass().getName() + "#\n");

	         // Check for and process NE and NW subtrees
	         if ( sRoot.getClass().getName().equals("prQuadtree$prQuadInternal") ) {
	            prQuadInternal p = (prQuadInternal) sRoot;
	            printTreeHelper(Out, p.NE, Padding + pad);
	            printTreeHelper(Out, p.NW, Padding + pad);
	         }
	      }
	      catch ( IOException e ) {
	         return;
	      }
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