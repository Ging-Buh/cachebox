package CB_Utils.Lists;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A resizable, ordered array. Avoids the boxing that occurs with ArrayList<Float>. If unordered, this class avoids a memory copy when
 * removing elements (the last element is moved to the removed element's position).
 * 
 * @author Longri, based on FloatArray from Nathan Sweet (LibGdx)
 */
public class CB_List<T> implements Iterable<T>, Serializable
{
	private static final long serialVersionUID = 4378819539487000418L;
	private T[] items;
	protected int size;
	private final int INITIAL_SIZE = 16;

	/** Creates an ordered array with a capacity of 16. */
	public CB_List()
	{
		items = this.createNewItems(INITIAL_SIZE);
	}

	/** Creates an ordered array with the specified capacity. */
	public CB_List(int capacity)
	{
		items = this.createNewItems(capacity);
	}

	/**
	 * Creates a new array containing the elements in the specific array. The new array will be ordered if the specific array is ordered.
	 * The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be grown.
	 */
	public CB_List(CB_List<T> array)
	{
		size = array.size;
		items = this.createNewItems(size);
		System.arraycopy(array.items, 0, items, 0, size);
	}

	public CB_List(T[] values)
	{
		size = values.length - 1;
		items = values;
	}

	@SuppressWarnings("unchecked")
	protected T[] createNewItems(int size)
	{
		return (T[]) new Object[size];
	}

	public void add(int index, T t)
	{
		if (size == items.length) ensureCapacity(size + 1);
		if (index != size) System.arraycopy(items, index, items, index + 1, size - index);
		items[index] = t;
		size++;
	}

	public int add(T value)
	{
		if (size == this.items.length) resize(size + (size >> 1));
		int ID = size;
		this.items[size++] = value;
		return ID;
	}

	public void addAll(CB_List<T> array)
	{
		addAll(array, 0, array.size);
	}

	public void addAll(int index, CB_List<T> array)
	{
		Iterator<T> itr = array.iterator();
		int csize = array.size();

		if (csize + size > items.length) ensureCapacity(size + csize);
		int end = index + csize;
		if (size > 0 && index != size) System.arraycopy(items, index, items, end, size - index);
		size += csize;
		for (; index < end; index++)
			items[index] = itr.next();
	}

	public void addAll(CB_List<T> array, int offset, int length)
	{
		if (offset + length > array.size) throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length
				+ " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll(T... array)
	{
		addAll(array, 0, array.length);
	}

	public void addAll(T[] array, int offset, int length)
	{
		int sizeNeeded = (size + length);
		if (sizeNeeded > items.length) resize(sizeNeeded + (size >> 1));
		System.arraycopy(array, offset, this.items, size, length);
		size += length;
	}

	public T get(int index)
	{
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		return items[index];
	}

	public boolean contains(T value)
	{
		return indexOf(value) >= 0;
	}

	public int indexOf(T value)
	{
		T[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			if (items[i].equals(value)) return i;
		return -1;
	}

	public T remove(T value)
	{
		T[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
		{
			if (items[i] == value)
			{
				return remove(i);
			}
		}
		return null;
	}

	/** Removes and returns the item at the specified index. */
	public T remove(int index)
	{
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		T[] items = this.items;
		T value = items[index];
		size--;
		System.arraycopy(items, index + 1, items, index, size - index);
		return value;
	}

	/**
	 * Removes from this array all of elements contained in the specified array.
	 * 
	 * @return true if this array was modified.
	 */
	public boolean removeAll(CB_List<T> array)
	{
		int size = this.size;
		int startSize = size;
		T[] items = this.items;
		for (int i = 0, n = array.size; i < n; i++)
		{
			T item = array.get(i);
			for (int ii = 0; ii < size; ii++)
			{
				if (item == items[ii])
				{
					remove(ii);
					size--;
					break;
				}
			}
		}
		return size != startSize;
	}

	/** Removes and returns the last item. */
	public T pop()
	{
		return items[--size];
	}

	/** Returns the last item. */
	public T peek()
	{
		return items[size - 1];
	}

	/** Returns the first item. */
	public T first()
	{
		if (size == 0) throw new IllegalStateException("Array is empty.");
		return items[0];
	}

	public void clear()
	{
		size = 0;
	}

	/**
	 * Reduces the size of the backing array to the size of the actual items. This is useful to release memory when many items have been
	 * removed, or if it is known that more items will not be added.
	 */
	public void shrink()
	{
		if (items.length == size) return;
		resize(size);
	}

	/**
	 * Increases the size of the backing array to acommodate the specified number of additional items. Useful before adding many items to
	 * avoid multiple backing array resizes.
	 * 
	 * @return {@link #items}
	 */
	public T[] ensureCapacity(int additionalCapacity)
	{
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded > items.length) resize(Math.max(INITIAL_SIZE, sizeNeeded));
		return items;
	}

	protected T[] resize(int newSize)
	{
		if (newSize < INITIAL_SIZE) newSize = INITIAL_SIZE;
		if (this.items.length == 0) return this.items = createNewItems(newSize);

		{// TODO Replace with this.items=Arrays.copyOf(this.items, newSize); if SDK>9
			T[] tmp = createNewItems(newSize);
			System.arraycopy(this.items, 0, tmp, 0, this.items.length); // Arrays.copyOf(this.items, newSize);
			this.items = tmp;
		}

		return this.items;
	}

	/**
	 * Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is taken.
	 */
	public void truncate(int newSize)
	{
		if (size > newSize) size = newSize;
	}

	/** Returns a random item from the array, or zero if the array is empty. */
	public T random()
	{
		if (size == 0) return null;
		return items[(int) (Math.random() * (size - 1))];
	}

	public T[] toArray()
	{
		T[] array = this.createNewItems(size);
		System.arraycopy(items, 0, array, 0, size);
		return array;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object == this) return true;
		if (!(object instanceof CB_List)) return false;
		@SuppressWarnings("unchecked")
		CB_List<T> array = (CB_List<T>) object;
		int n = size;
		if (n != array.size) return false;
		for (int i = 0; i < n; i++)
			if (items[i] != array.items[i]) return false;
		return true;
	}

	@Override
	public String toString()
	{
		if (size == 0) return "[]";
		T[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(items[0]);
		for (int i = 1; i < size; i++)
		{
			buffer.append(", ");
			buffer.append(items[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	public String toString(String separator)
	{
		if (size == 0) return "";
		T[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append(items[0]);
		for (int i = 1; i < size; i++)
		{
			buffer.append(separator);
			buffer.append(items[i]);
		}
		return buffer.toString();
	}

	public int size()
	{
		return size;
	}

	public boolean isEmpty()
	{
		return size <= 0;
	}

	@Override
	public Iterator<T> iterator()
	{
		Itr itr = new Itr();
		itr.reverse = reverse;
		return new Itr();
	}

	/**
	 * An optimized version of AbstractList.Itr
	 */
	private class Itr implements Iterator<T>
	{

		protected boolean reverse = false;
		int cursor; // index of next element to return

		@Override
		public boolean hasNext()
		{
			return cursor != size;
		}

		@Override
		public T next()
		{
			if (cursor >= size) throw new NoSuchElementException();
			if (cursor >= items.length) throw new ConcurrentModificationException();

			int index = reverse ? size - cursor++ : cursor++;
			return items[index];
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("remove is not supported by this Iterator");

		}

	}

	public void sort()
	{
		if (size == 0) return;
		Arrays.sort(items, 0, size);
	}

	public void set(int index, T value)
	{
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		items[index] = value;
	}

	boolean reverse = false;

	public CB_List<T> reverse()
	{
		reverse = true;
		return this;
	}

}