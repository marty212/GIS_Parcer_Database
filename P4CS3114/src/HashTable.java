import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * My hashtable.
 * @author Martin Borstad
 * @project GIS System
 * @version 04.16.2013
 */
public class HashTable<T> {
	private final int CAPACITY[]= {1019, 2027, 4079, 8123, 16267, 32503, 65011, 130027, 260111, 520279, 1040387, 2080763, 4161539, 8323151, 16646323};
	
	private int       capacity; //The array capacity
	private int       size;     //The elements in array
	private T         array[];  //The array
	private int       step;     //next element in array;
	
	/**
	 * Makes the initial hashtable.
	 */
	@SuppressWarnings("unchecked")
	public HashTable()
	{
		step = 0;
		array = (T[]) new Object[CAPACITY[step]];
		capacity = CAPACITY[step];
		step++;
		size = 0;
	}
	
	/**
	 * Adds an element to the hashtable.
	 * @param elem the element being added
	 * @return the probe sequence
	 */
	public int add(T elem)
	{
		int i = elem.hashCode();
		int n = 0;
		while(true)
		{
			int pos = (i + (n*n + n) / 2) % capacity;
			if(array[pos] == null)
			{
				size++;
				array[pos] = elem;
				break;
			}
			else
			{
				n++;
			}
		}
		if(capacity*.7 <= size)
		{
			expand();
		}
		return n;
	}
	
	/**
	 * Expands the table to double the current size.
	 * Also reinserts everything.
	 */
	@SuppressWarnings("unchecked")
	private void expand()
	{
		T[] h = array;
		if(step >= CAPACITY.length)
			capacity = 2 * capacity;
		else
		{
			capacity = CAPACITY[step];
			step++;
		}
		array =  (T[]) new Object[capacity];
		for (T a : h)
		{
			if (a != null)
			{
				int i = a.hashCode() % capacity;
				int n = 0;
				while(true)
				{
					int pos = (i + (n*n + n) / 2) % capacity;
					if(array[pos] == null)
					{
						array[pos] = a;
						break;
					}
					else
					{
						n++;
					}
				}
			}
		}
	}
	
	/**
	 * Gets similar elements (on hash).
	 * @param elem the element being compared
	 * @return the similar elements
	 */
	public T retrive(T elem)
	{
		int i = elem.hashCode() % capacity;
		int n = 0;
		while(true)
		{
			int pos = (i + (n*n + n) / 2) % capacity;
			if (array[pos] == null)
			{
				return null;
			}
			else if(array[pos].equals(elem))
			{
				return array[pos];
			}
			else
			{
				n++;
			}
		}
	}
	
	/**
	 * Prints out the hash table.
	 * @param file the log being written to
	 */
	public void printHash(RandomAccessFile file)
	{
		try {
			file.writeBytes("Format of display is\nSlot Number : Data Record\nCurrent Table size is " +
					capacity + "\nNumber of elements in the table is " + size + "\n\n");
			/*
			 * Run through the table and print the spots that have stuff in them.
			 */
			for(int i = 0; i < capacity; i++)
			{
				if (array[i] != null)
				{
					file.writeBytes(i + ":  " + array[i].toString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
