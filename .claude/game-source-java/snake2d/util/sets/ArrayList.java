package snake2d.util.sets;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;

/**
 * I got pissed that java doesn't have a inheritance structure that allows for
 * unmodifiable collections, so I made my own arraylist. this list is fixed in size.
 * the order of the elemets put in will not be the order of the content. It is not threadable.
 * A neat thing is that you can remove/add while iterating. It is serilizable.
 * @author mail__000
 *
 * @param <E>
 */
public class ArrayList<E> implements LISTE<E>, SAVABLE, Serializable{


	
	private static final long serialVersionUID = 1L;
	private final Object[] es;
	private final Iter<E> iterator = new Iter<E>();
	private final int size;
	private int last = 0;
	
	/**
	 * 
	 * @param size the fixed size of the list. Connot be changed.
	 */
	public ArrayList(int size){
		this.size = size;
		es = new Object[size];
	}
	
	public ArrayList(E first){
		
		this.size = 1;
		es = new Object[1];
		last = size;
		es[0] = first;
	}
	
	@SuppressWarnings("unused")
	public <T extends E> ArrayList(Iterable<T> other){
		int i = 0;
		for (E e : other)
			i ++;
		es = new Object[i];
		i = 0;
		for (E e : other){
			es[i] = e;
			i++;
		}
		size = i;
		last = size;
	}
	
	@SafeVarargs
	public <T extends E> ArrayList(T... es){
		this.es = es;
		this.size = es.length;
		last = size;
	}
	
	public int indexOf(E t) {
		for (int i = 0; i < last; i++){
			if (es[i] == t)
				return i;
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		if (index < this.last)
			return (E)es[index];
		return null;
	}

	@SuppressWarnings("unchecked")
	public E last(){
		if (size() > 0)
			return (E)es[last-1];
		return null;
	}
	
	/**
	 * 
	 * @param e
	 * @return index if successful, -1 if not (set full)
	 */
	@Override
	public int add(E e){
		if (!hasRoom())
			throw new RuntimeException("I'm full!" + " "  + last  + " " + size);
		es[last] = e;
		last++;
		return last -1;
	}
	
	public void addNull() {
		if (!hasRoom())
			throw new RuntimeException("I'm full!");
		es[last] = null;
		last++;
	}
	
	@Override
	public int tryAdd(E e) {
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
		
		if (i == last-1){
			@SuppressWarnings("unchecked")
			E e = (E) es[last-1];
			es[last-1] = null;
			last --;
			return e;
		}
		
		@SuppressWarnings("unchecked")
		E e = (E) es[i];
		es[i] = es[last-1];
		es[last-1] = null;
		last --;
		if (iterator.current == i+1)
			iterator.current --;
		return e;
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
	public E remove(E object){
		for (int i = 0; i < last; i++){
			if (es[i] == object)
				return remove(i);
		}
		return null;
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
	
	public int remainingSlots() {
		return size-last;
	}

	@Override
	public boolean hasRoom() {
		return remainingSlots() > 0;
	}

	@Override
	public boolean contains(int i) {
		return i < size && es[i] != null;
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
	@Override
	public void clear(){
		if (last > es.length)
			last = es.length;
		for (int i = 0; i < last; i++){
			es[i] = null;
		}
		last = 0;
	}
	
	public void clearSloppy() {
		last = 0;
	}
	
	@Override
	public int size() {
		return last;
	}
	
	public int max(){
		return size;
	}
	
	@Override
	public Iter<E> iterator() {
		iterator.init();
		return iterator;
	}

	public void iteratorRemoveCurrent(){
		remove(iterator.current -1);
	}
	
	public class Iter<T> implements Iterator<E>, Serializable{

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

	@Override
	public boolean isEmpty() {
		return last == 0;
	}

	public E removeLast() {
		return remove(size()-1);
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
		
		Arrays.sort(es, 0, last, co);
	}
	
	public void shiftLeft() {
		for (int i = 1; i < last; i++) {
			es[i-1] = es[i];
		}
		es[last-1] = null;
		last --;
	}
	
	public void shiftLeft(int index) {
		for (int i = index+1; i < last; i++) {
			es[i-1] = es[i];
		}
		es[last-1] = null;
		last --;
	}

	@Override
	public void save(FilePutter file) {
		file.i(size());
		for (E e : this)
			file.object(e);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		last = file.i();
		for (int i = 0; i < last; i++)
			es[i] = file.object(false);
		
	}

	public void replace(int index, E object) {
		es[index] = object;
		if (index >= last)
			last = index+1;
	}

	public ArrayList<E> reverse() {
		int s = size()/2;
		for (int i = 0; i < s; i++) {
			Object o = es[i];
			es[i] = es[size()-1-i];
			es[size()-1-i] = o;
		}
		return this;
	}

	public void insert(int i, E object) {
		if (!hasRoom())
			throw new RuntimeException();
		if (i < 0 || i > last)
			throw new RuntimeException();
		for (int k = last; k > i; k--)
			es[k] = es[k-1];
		es[i] = object;
		last++;
	}

	public void swap(int position, int position2) {
		Object e = es[position];
		es[position] = es[position2];
		es[position2] = e;
	}
	
	public void shuffle() {
		
		if (size() <= 1)
			return;
		
		for (int i = 0; i < size(); i++) {
			swap(i, RND.rInt(size()));
		}
		
	}
	
}
