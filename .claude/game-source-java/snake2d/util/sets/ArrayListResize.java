package snake2d.util.sets;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayListResize<E> implements LISTE<E>, Serializable{
	
	private static final long serialVersionUID = 1L;
	private Object[] es;
	private final Iter<E> iterator = new Iter<E>();
	private final IterReverse<E> iteratorReverse = new IterReverse<E>();
	private final int maxSize;
	private final int minSize;
	private int last = 0;
	
	/**
	 * 
	 * @param maxSize the fixed size of the list. Connot be changed.
	 */
	public ArrayListResize(int minSize, int maxSize){
		this.maxSize = maxSize;
		this.minSize = minSize;
		es = new Object[minSize];
	}
	
	public ArrayListResize(int minSize){
		this.maxSize = Integer.MAX_VALUE;
		this.minSize = minSize;
		es = new Object[minSize];
	}
	
	private void increase(){
		
		if (last == es.length-1 && es.length != maxSize){
			int size = es.length*2;
			if (size > maxSize)
				size = maxSize;
			Object[] esNew = new Object[size];
			for (int i = 0; i < last; i++){
				esNew[i] = es[i];
			}
			es = esNew;
		}
	}
	
	private void decrease(){
		if (es.length != minSize && last < es.length/2){
			int size = es.length/2;
			if (size < minSize)
				size = minSize;
			Object[] esNew = new Object[size];
			for (int i = 0; i < last; i++){
				esNew[i] = es[i];
			}
			es = esNew;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		if (index < this.last)
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
		int i = tryAdd(e);
		if (i == -1)
			throw new RuntimeException(""+hasRoom());
		return i;
	}
	
	@Override
	public int tryAdd(E e) {
		increase();
		if (!hasRoom())
			return -1;
		es[last] = e;
		last++;
		return last -1;
	}

	/**
	 * removes and messes up the order.
	 * @param i
	 * @return if success
	 */
	public E remove(int i){
		
		if (i >= last)
			return null;
		
		@SuppressWarnings("unchecked")
		E e = (E) es[i];
		
		if (i == last-1){
			es[last-1] = null;
			last --;
			decrease();
			return e;
		}
		
		es[i] = es[last-1];
		es[last-1] = null;
		last --;
		if (iterator.current == i+1)
			iterator.current --;
		decrease();
		return e;
	}
	
	public int removeOrdered(E object){
		for (int i = 0; i < last; i++){
			if (es[i] == object) {
				removeOrdered(i);
				return i;
			}
		}
		return -1;
	}
	
	public E removeOrdered(int i){

		if (i >= last)
			return null;
		
		if (i == last-1){
			@SuppressWarnings("unchecked")
			E e = (E) es[last-1];
			es[last-1] = null;
			last --;
			return e;
		}
		
		@SuppressWarnings("unchecked")
		E e = (E) es[i];
		
		for (int k = i; k < last-1; k++) {
			es[k] = es[k+1];
		}

		es[last-1] = null;
		last --;
		if (iterator.current >= i)
			iterator.current --;
		return e;
	}
	
	/**
	 * removes and messes up the order.
	 * @param object
	 * @return if success
	 */
	public boolean remove(E object){
		for (int i = 0; i < last; i++){
			if (es[i] == object)
				return remove(i) != null;
		}
		return false;
	}
	
	public int remainingSlots() {
		return maxSize-last;
	}

	@Override
	public boolean hasRoom() {
		return remainingSlots() > 0;
	}

	@Override
	public boolean contains(int i) {
		return i < last;
	}
	
	@Override
	public boolean contains(E object) {
		
		for (int i = 0; i < last; i++){
			if (es[i] == object)
				return true;
		}
		return false;
		
	}
	
	/**
	 * Start anew!
	 */
	public void clear(){
		if (es.length != minSize)
			es = new Object[minSize];
		last = 0;
	}
	
	public void clearSoft(){
		last = 0;
	}
	
	@Override
	public int size() {
		return last;
	}
	
	public int max(){
		return maxSize;
	}
	
	@Override
	public Iter<E> iterator() {
		iterator.init();
		return iterator;
	}

	public Iterator<E> iteratorReverse(){
		iteratorReverse.init();
		return iteratorReverse;
	}
	
	public void iteratorRemoveCurrent(){
		remove(iterator.current -1);
	}
	
	private class Iter<T> implements Iterator<E>, Serializable{

		private static final long serialVersionUID = 1L;
		private int current;
		
		private void init(){
			current = 0;
		}
		
		@Override
		public boolean hasNext() {
			return current < last;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			return (E) es[current++];
		}
		
		
	}

	private class IterReverse<T> implements Iterator<E>, Serializable{

		private static final long serialVersionUID = 1L;
		private int current;
		
		private void init(){
			current = last -1;
		}
		
		@Override
		public boolean hasNext() {
			return current >= 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			return (E) es[current--];
		}
		
	}
	
	@Override
	public boolean isEmpty() {
		return last == 0;
	}
	
	
	public void trim(){
		Object[] no = new Object[last];
		for (int i = 0; i < last; i++)
			no[i] = es[i];
		es = no;
	}

	public E getLast() {
		return get(last-1);
	}
	
	public void sort(Comparator<E> c){
		if (last <= 1)
			return;
		
		Comparator<Object> co = new Comparator<Object>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Object o1, Object o2) {
				return c.compare((E)o1, (E)o2);
			}
		};
		
		Arrays.sort(es, 0, last-1, co);
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

	
}
