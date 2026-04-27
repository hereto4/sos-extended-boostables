package snake2d.util.sets;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayListGrower<E> implements LISTE<E>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object[] es = new Object[0];
	private final Iter<E> iterator = new Iter<E>();
	
	/**
	 * 
	 * @param maxSize the fixed size of the list. Connot be changed.
	 */
	public ArrayListGrower(){

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		if (index < es.length)
			return (E)es[index];
		throw new NoSuchElementException("no element at index: " + index);
	}

	/**
	 * 
	 * @param e
	 * @return true if success (not full)
	 */
	@Override
	public int add(E e){
		return tryAdd(e);
	}
	
	@Override
	public int tryAdd(E e) {
		
		Object[] nn = new Object[es.length+1];
		for (int i = 0; i < es.length; i++)
			nn[i] = es[i];
		
		nn[es.length] = e;
		es = nn;
		return es.length-1;
	}
	
	@Override
	public boolean hasRoom() {
		return true;
	}

	@Override
	public boolean contains(int i) {
		return i < es.length;
	}
	
	@Override
	public boolean contains(E object) {
		
		return firstIndexOf(object) >= 0;
		
	}
	
	/**
	 * Start anew!
	 */
	public void clear(){
		es = new Object[0];
	}

	
	@Override
	public int size() {
		return es.length;
	}
	
	@Override
	public Iter<E> iterator() {
		iterator.init();
		return iterator;
	}
	
	private class Iter<T> implements Iterator<E>, Serializable{

		private static final long serialVersionUID = 1L;
		private int current;
		
		private void init(){
			current = 0;
		}
		
		@Override
		public boolean hasNext() {
			return current < es.length;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			return (E) es[current++];
		}
		
		
	}
	
	@Override
	public boolean isEmpty() {
		return es.length == 0;
	}

	
	public void swap(int indexA, int indexB) {
		if (indexA < 0 || indexB >= size() || indexB < 0 || indexB >= size())
			throw new RuntimeException();
		Object a = es[indexA];
		es[indexA] = es[indexB];
		es[indexB] = a;
	}
	
	public void shiftRight() {
		if (isEmpty())
			return;
		E e = get(size()-1);
		for (int i = size()-1; i > 0; i--) {
			es[i] = es[i-1];
		}
		es[0] = e;
	}

	public void replace(int index, E e2) {
		es[index] = e2;
	}

	public boolean remove(E remove) {
		int i = firstIndexOf(remove);
		if (i < 0)
			return false;
		remove(i);
		return true;
	}
	
	public int firstIndexOf(E e) {
		for (int i = 0; i < es.length; i++){
			if (es[i] == e)
				return i;
		}
		return -1;
	}
	
	public void remove(int index) {
		if (index < 0 || index >= size())
			throw new RuntimeException("" + index);
		Object[] nn = new Object[es.length-1];
		
		int oi = 0;
		for (int i = 0; i < es.length; i++) {
			if (i == index)
				continue;
			nn[oi] = es[i];
			oi++;
		}
		es = nn;
	}
	
}
