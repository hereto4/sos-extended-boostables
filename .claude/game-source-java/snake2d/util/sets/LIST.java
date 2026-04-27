package snake2d.util.sets;


import snake2d.util.rnd.RND;

/**
 * An unmodifiable "arraylist"
 * @author mail__000
 *
 * @param <E>
 */
public interface LIST<E> extends Iterable<E>{

	/**
	 * if there exists something at this index, you'll get it.
	 * @param index
	 * @return
	 */
	public E get(int index);
	
	public default E getC(int index) {
		if (size() == 0)
			return null;
		int remainder = (index % size());
		index = ((remainder >> 31) & size()) + remainder;
		return get(index);
	}
	
	public default E rnd() {
		return get(RND.rInt(size()));
	}
	
	/**
	 * is there an object at given index? 1 complexity.
	 * @param i
	 * @return
	 */
	public boolean contains(int i);
	/**
	 * Does this contain this object? N complexity.
	 * @param object
	 * @return
	 */
	public boolean contains(E object);
	/**
	 * 
	 * @return the nr of elements this list contains
	 */
	public int size();
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty();
	
	public default <T extends E> LIST<E> join(LIST<T> other){
		ArrayList<E> n = new ArrayList<>(size()+other.size());
		for (int i = 0; i < size(); i++)
			n.add(get(i));
		for (int i = 0; i < other.size(); i++)
			n.add(other.get(i));
		return n;
	}
	
	public default <T extends E> LIST<E> join(@SuppressWarnings("unchecked") T... others){
		ArrayList<E> n = new ArrayList<>(size()+others.length);
		for (int i = 0; i < size(); i++)
			n.add(get(i));
		for (int i = 0; i < others.length; i++)
			n.add(others[i]);
		return n;
	}
	
}
